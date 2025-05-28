package com.wang.wangaiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author: Shajia Wang
 * @createTime: 2025/5/22---15:42
 * @description:
 */

@SpringBootTest
class LoveAppDocumentLoaderTest {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Qualifier("pgVectorVectorStore")
    @Autowired
    private VectorStore pgVectorVectorStore;

    @Test
    void load(){
        List<Document> documentList = loveAppDocumentLoader.loadMarkdowns();
        pgVectorVectorStore.add(documentList);
    }
}