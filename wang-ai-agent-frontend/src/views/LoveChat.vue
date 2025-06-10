<template>
  <div class="chat-container">
    <h2>AI 恋爱大师</h2>
    <div class="chat-history">
      <div v-for="(msg, idx) in messages" :key="idx" :class="msg.role === 'user' ? 'msg-user' : 'msg-ai'">
        <template v-if="msg.role === 'ai'">
          <img class="avatar" src="/ai-love-avatar.png" alt="AI头像" />
          <div class="msg-content ai-content">{{ msg.content }}</div>
        </template>
        <template v-else>
          <div class="msg-content user-content">{{ msg.content }}</div>
        </template>
      </div>
    </div>
    <div class="chat-input">
      <input v-model="input" @keyup.enter="sendMessage" placeholder="输入你的消息..." />
      <button @click="sendMessage">发送</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const messages = ref([]);
const input = ref('');
const chatId = ref('');

function genId() {
  return 'chat_' + Math.random().toString(36).slice(2, 10);
}

onMounted(() => {
  chatId.value = genId();
});

function sendMessage() {
  if (!input.value.trim()) return;
  const userMsg = { role: 'user', content: input.value };
  messages.value.push(userMsg);
  const currentChatId = chatId.value;
  const eventSource = new EventSource(
    `http://localhost:8123/api/ai/love_app/chat/sse?message=${encodeURIComponent(input.value)}&chatId=${currentChatId}`
  );
  let aiMsg = { role: 'ai', content: '' };
  messages.value.push(aiMsg);
  eventSource.onmessage = (e) => {
    aiMsg.content += e.data;
    messages.value = [...messages.value];
  };
  eventSource.onerror = () => {
    eventSource.close();
  };
  input.value = '';
}
</script>

<style scoped>
.chat-container {
  max-width: 700px;
  margin: 48px auto;
  border: 1px solid #eee;
  border-radius: 16px;
  padding: 36px 28px 24px 28px;
  background: #fff;
  box-shadow: 0 4px 24px rgba(0,0,0,0.08);
}
.chat-history {
  min-height: 320px;
  max-height: 480px;
  overflow-y: auto;
  margin-bottom: 24px;
  padding: 16px 10px;
  background: #f8fafc;
  border-radius: 10px;
  border: 1px solid #f0f0f0;
}
.msg-user {
  display: flex;
  justify-content: flex-end;
  margin: 18px 0 18px 0;
}
.msg-ai {
  display: flex;
  align-items: flex-start;
  margin: 18px 0 18px 0;
}
.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin-right: 16px;
  background: #e6f7ff;
  object-fit: cover;
  box-shadow: 0 1px 4px rgba(66,185,131,0.08);
}
.msg-content {
  display: inline-block;
  padding: 14px 22px;
  border-radius: 18px;
  background: #e6f7ff;
  color: #333;
  max-width: 70vw;
  word-break: break-all;
  font-size: 1.08rem;
  line-height: 1.7;
  box-shadow: 0 2px 8px rgba(66,185,131,0.04);
}
.user-content {
  background: #c1e2b3;
  color: #222;
}
.ai-content {
  background: #fff;
  border: 1px solid #42b983;
  color: #222;
  text-align: left;
}
.chat-input {
  display: flex;
  gap: 12px;
  margin-top: 18px;
}
.chat-input input {
  flex: 1;
  padding: 12px;
  border-radius: 6px;
  border: 1px solid #ccc;
  font-size: 1.08rem;
}
.chat-input button {
  padding: 12px 28px;
  border: none;
  border-radius: 6px;
  background: #42b983;
  color: #fff;
  cursor: pointer;
  font-size: 1.08rem;
  transition: background 0.2s;
}
.chat-input button:hover {
  background: #369e6f;
}
@media (max-width: 900px) {
  .chat-container {
    max-width: 98vw;
    padding: 10px;
  }
  .msg-content {
    max-width: 90vw;
    font-size: 1rem;
  }
}
@media (max-width: 600px) {
  .chat-container {
    margin: 10px auto;
    padding: 4px;
  }
  .chat-history {
    min-height: 180px;
    max-height: 260px;
    padding: 2px;
  }
  .msg-content {
    font-size: 0.97rem;
    padding: 9px 12px;
  }
  .avatar {
    width: 28px;
    height: 28px;
    margin-right: 8px;
  }
}
</style> 