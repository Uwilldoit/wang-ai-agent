package com.wang.wanghowtocookmcpserver.tools;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: Shajia Wang
 * @createTime: 2025/6/3---15:41
 * @description: 菜谱搜索工具
 */


@Service
public class HowToCookTool {
    private static final Logger log = LoggerFactory.getLogger(HowToCookTool.class);


    /**
     * 内存中的菜谱数据库
     */
    private static final List<Recipe> RECIPES = new ArrayList<>();

    static {
        // 初始化菜谱数据 - 这里使用您提供的JSON数据
        String jsonData = null;
        try {
            jsonData = HttpUtil.get("https://weilei.site/all_recipes.json");
        } catch (Exception e) {
            log.error("从远程获取菜谱数据失败", e);
        }
        // 判断是否获取到HTML内容（如错误页面）
        if (StrUtil.isBlank(jsonData) || StrUtil.startWith(jsonData, "<html>")) {
            try {
                jsonData = ResourceUtil.readUtf8Str("howToCook-data.json");
                if (StrUtil.isBlank(jsonData)) {
                    log.warn("本地JSON文件内容为空");
                }
            } catch (Exception e) {
                log.error("读取本地JSON文件失败", e);
            }
        }

        if (StrUtil.isNotBlank(jsonData)) {
            try {
                JSONArray array = JSONUtil.parseArray(jsonData);
                for (int i = 0; i < array.size(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    RECIPES.add(parseRecipe(obj));
                }
                } catch(Exception e){
                    log.error("解析JSON数据失败", e);
                }
            } else{
                log.error("无法获取有效的JSON数据");
            }
        }

    /**
     * 智能搜索菜谱 - 主搜索接口
     *
     * @param query 搜索查询（可包含多个关键词）
     * @return 匹配的菜谱列表，按相关度排序
     */
    @Tool(description = "Search recipes based on keywords")
    public static List<Recipe> searchRecipes(@ToolParam(description = "Search query keywords") String query) {
        if (StrUtil.isBlank(query)) {
            return new ArrayList<>();
        }

        // 多维度匹配搜索
        return RECIPES.stream()
                .map(recipe -> {
                    recipe.matchScore = calculateMatchScore(recipe, query);
                    return recipe;
                })
                .filter(recipe -> recipe.matchScore > 0)
                .sorted(Comparator.comparingDouble((Recipe r) -> r.matchScore).reversed())
                .limit(20)
                .collect(Collectors.toList());
    }

    /**
     * 按ID获取菜谱详情
     *
     * @param id 菜谱ID
     * @return 菜谱对象，未找到返回null
     */
    public static Recipe getRecipeById(String id) {
        return RECIPES.stream()
                .filter(recipe -> recipe.id.equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * 计算匹配分数（0.0-1.0）
     */
    private static double calculateMatchScore(Recipe recipe, String query) {
        double score = 0;
        String[] keywords = query.toLowerCase().split("\\s+");

        // 名称匹配（最高权重）
        score += matchField(recipe.name, keywords, 0.4);

        // 食材匹配（高权重）
        score += recipe.ingredients.stream()
                .mapToDouble(ingredient -> matchField(ingredient.name, keywords, 0.3))
                .sum();

        // 描述匹配（中等权重）
        score += matchField(recipe.description, keywords, 0.2);

        // 分类和标签匹配（低权重）
        score += matchField(recipe.category, keywords, 0.05);
        score += matchField(String.join(" ", recipe.tags), keywords, 0.05);

        return Math.min(score, 1.0);
    }

    /**
     * 字段匹配计算
     */
    private static double matchField(String field, String[] keywords, double weight) {
        if (StrUtil.isBlank(field)) {
            return 0;
        }

        String lowerField = field.toLowerCase();
        int matchCount = 0;

        for (String keyword : keywords) {
            if (lowerField.contains(keyword)) {
                matchCount++;
            }
        }

        return (double) matchCount / keywords.length * weight;
    }

    /**
     * 解析JSON到Recipe对象
     */
    private static Recipe parseRecipe(JSONObject obj) {
        Recipe recipe = new Recipe();
        recipe.id = obj.getStr("id");
        recipe.name = obj.getStr("name");
        recipe.description = obj.getStr("description");
        recipe.category = obj.getStr("category");
        recipe.difficulty = obj.getInt("difficulty", 0);
        recipe.tags = obj.getJSONArray("tags").toList(String.class);

        // 解析食材
        JSONArray ingredients = obj.getJSONArray("ingredients");
        recipe.ingredients = new ArrayList<>();
        for (int i = 0; i < ingredients.size(); i++) {
            JSONObject ing = ingredients.getJSONObject(i);
            Ingredient ingredient = new Ingredient();
            ingredient.name = ing.getStr("name");
            recipe.ingredients.add(ingredient);
        }

        // 解析步骤
        JSONArray steps = obj.getJSONArray("steps");
        recipe.steps = new ArrayList<>();
        for (int i = 0; i < steps.size(); i++) {
            JSONObject step = steps.getJSONObject(i);
            recipe.steps.add(step.getStr("description"));
        }

        return recipe;
    }

    /**
     * 菜谱数据模型 - 为AI优化
     */
    public static class Recipe {
        public String id;
        public String name;
        public String description;
        public String category;
        public int difficulty;
        public List<String> tags;
        public List<Ingredient> ingredients;
        public List<String> steps;
        public double matchScore; // AI排序用

        // 获取简化信息（供AI选择时使用）
        public String getSummary() {
            return StrUtil.format("【{}】{} - 难度{}星 - {}种食材",
                    name, StrUtil.subPre(description, 40), difficulty, ingredients.size());
        }
    }

    /**
     * 食材数据模型
     */
    public static class Ingredient {
        public String name;
    }
}

