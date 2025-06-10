package com.wang.wangaiagent.app;

import com.wang.wangaiagent.advisor.MyLoggerAdvisor;
import com.wang.wangaiagent.advisor.ReReadingAdvisor;
import com.wang.wangaiagent.manager.mysql.DatabaseChatMemory;
import com.wang.wangaiagent.rag.LoveAppRagCustomAdvisorFactory;
import com.wang.wangaiagent.rag.QueryRewriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * @author: Shajia Wang
 * @createTime: 2025/5/10---23:01
 * @description:
 */
@Component
@Slf4j
public class LoveApp {

    private final ChatClient chatClient;

    public static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。\" +\n" +
            "            \"围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；\" +\n" +
            "            \"恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。\" +\n" +
            "            \"引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";
    @Autowired
    private Advisor loveAppRagCloudAdvisor;

    @Qualifier("pgVectorVectorStore")
    @Autowired
    private VectorStore pgVectorVectorStore;


    /**
     * 初始化 ChatClient
     * @param dashscopeChatModel 阿里大模型对象
     */
    public LoveApp(ChatModel dashscopeChatModel) {
        // 初始化基于文件的对话记忆
//        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
//        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
        // 初始化基于文件的对话记忆
        InMemoryChatMemory chatMemory  = new InMemoryChatMemory();
        //构建一个带有默认系统提示和记忆顾问的ChatClient
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory),
                        // 自定义日志 Advisor，可按需开启
                        new MyLoggerAdvisor(),
                        //自定义推理增强Advisor，可按需开启
                        new ReReadingAdvisor())
                .build();
    }

//    /**
//     * 初始化 ChatClient
//     * @param dashscopeChatModel 阿里大模型对象
//     * @param databaseChatMemory 数据库对话记忆
//     */
//    public LoveApp(ChatModel dashscopeChatModel, DatabaseChatMemory databaseChatMemory) {
//        // 初始化基于数据库的对话记忆
//        chatClient = ChatClient.builder(dashscopeChatModel)
//                .defaultSystem(SYSTEM_PROMPT)
//                .defaultAdvisors(
//                        new MessageChatMemoryAdvisor(databaseChatMemory),
//                        new MyLoggerAdvisor(),
//                        new ReReadingAdvisor()
//                )
//                .build();
//    }




    /**
     * 用于处理用户输入的聊天消息，调用聊天客户端向AI模型发起请求，并获取AI返回的响应内容
     * @param message 用户消息
     * @param chatId 会话ID
     * @return AI返回的响应内容
     */
    public String doChat(String message,String chatId){
        ChatResponse response = chatClient.prompt()
                .user(message)
                .advisors(advisorSpec -> advisorSpec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("AI Response: {}", content);
        return content;
    }

    /**
     * 定义恋爱报告类
     * @param title
     * @param suggestions
     */
    public record LoveReport(String title, List<String> suggestions) {
    }

    /**
     * 生成恋爱报告
     * @param message 用户消息
     * @param chatId 会话ID
     * @return 恋爱报告
     */
    public LoveReport doChatWithReport(String message,String chatId){
        LoveReport loveReport = chatClient.prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
                .user(message)
                .advisors(advisorSpec -> advisorSpec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, 10))
                .call()
                .entity(LoveReport.class);
        log.info("loveReport: {}", loveReport);
        return loveReport;
    }

    @Resource
    private VectorStore loveAppVectorStore;

    @Resource
    private QueryRewriter queryRewriter;

    /**
     * 和RAG知识库进行对话
     * @param message 用户消息
     * @param chatId 会话ID
     * @return AI返回的响应内容
     */
    public String doChatWithRag(String message,String chatId){
        message = queryRewriter.doQueryRewriter(message);
        ChatResponse response = chatClient
                .prompt()
                .user(message).
                advisors(advisorSpec -> advisorSpec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, 10))
                //开启日志，便于观察效果
//                .advisors(new MyLoggerAdvisor())
                //应用RAG知识库问答
//                .advisors(new QuestionAnswerAdvisor(loveAppVectorStore))
                //应用增强检索服务（云知识库服务）
//                .advisors(loveAppRagCloudAdvisor)
                //应用RAG检索增强服务（基于PgVector 向量存储）
                .advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
//                .advisors(LoveAppRagCustomAdvisorFactory.createLoveAppRagCustomAdvisor(
//                        loveAppVectorStore,"单身"
//                ))
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("AI Response: {}", content);
        return content;
    }


    @Resource
    private ToolCallback[] allTools;

    /**
     *   使用工具
     * @param message  用户消息
     * @param chatId   会话ID
     * @return  AI返回的响应内容
     */
    public String doChatWithTools(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 开启日志，便于观察效果
//                .advisors(new MyLoggerAdvisor())
                .tools(allTools)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }



    @Resource
    private ToolCallbackProvider toolCallbackProvider;

     /**
     *   使用MCP
     * @param message  用户消息
     * @param chatId   会话ID
     * @return  AI返回的响应内容
     */
    public String doChatWithMcp(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 开启日志，便于观察效果
//                .advisors(new MyLoggerAdvisor())
                .tools(toolCallbackProvider)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * 使用流式接口
     * @param message 用户消息
     * @param chatId 会话ID
     * @return 响应流
     * 不直接使用ChatResponse作为返回类型，因为会导致返回内容膨胀，影响传输效率
     */
    public Flux<String> doChatByStream(String message, String chatId){
        return chatClient.prompt()
                .user(message)
                .advisors(advisorSpec -> advisorSpec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY,10))
                .stream()
                .content();
    }









}
