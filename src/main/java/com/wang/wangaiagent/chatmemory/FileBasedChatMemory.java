package com.wang.wangaiagent.chatmemory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * @author: Shajia Wang
 * @createTime: 2025/5/11---16:34
 * @description: 基于文件持久化的对话记忆
 */

public class FileBasedChatMemory implements ChatMemory {
    private final String BASE_DIR;
    private static final Kryo kryo = new Kryo();

    static {
        kryo.setRegistrationRequired(false);
        //设置实例化策略
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    /**
     * 构造对象时，指定文件保存目录
     * @param dir 文件保存目录
     */
    public FileBasedChatMemory(String dir) {
        this.BASE_DIR = dir;
        File baseDir = new File(dir);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
    }

    /**
     * 添加对话记录
     * @param conversationId 会话ID
     * @param messages 对话记录
     */
    @Override
    public void add(String conversationId, List<Message> messages) {
        List<Message> conversationMessages = getOrCreateConversation(conversationId);
        conversationMessages.addAll(messages);
        saveConversation(conversationId, conversationMessages);
    }


    /**
     * 获取对话记录
     * @param conversationId 会话ID
     * @param lastN 获取最近N条记录
     * @return 对话记录
     */
    @Override
    public List<Message> get(String conversationId, int lastN) {
        List<Message> allMessages = getOrCreateConversation(conversationId);
        return allMessages.stream()
                .skip(Math.max(0, allMessages.size() - lastN))
                .toList();
    }

    /**
     * 清除对话记录
     * @param conversationId 会话ID
     */
    @Override
    public void clear(String conversationId) {
        File file = getConversationFile(conversationId);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     *根据会话ID加载对应的聊天消息列表。如果对应的文件存在，则使用Kryo反序列化读取消息列表
     * @param conversationId
     * @return
     */
    private List<Message> getOrCreateConversation(String conversationId) {
        File file = getConversationFile(conversationId);
        List<Message> messages = new ArrayList<>();
        if (file.exists()) {
            try (Input input = new Input(new FileInputStream(file))) {
                messages = kryo.readObject(input, ArrayList.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return messages;
    }

    /**
     * 根据会话ID生成对应的文件对象，文件位于指定的基础目录下，扩展名为.kryo。
     * @param conversationId 会话ID
     * @return 文件对象
     */
    private File getConversationFile(String conversationId) {
        return new File(BASE_DIR, conversationId + ".kryo");
    }

    /**
     * 将会话消息列表保存到对应的文件，使用Kryo序列化。
     * @param conversationId 会话ID
     */
    private void saveConversation(String conversationId, List<Message> messages) {
        File file = getConversationFile(conversationId);
        try (Output output = new Output(new FileOutputStream(file))) {
            kryo.writeObject(output, messages);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
