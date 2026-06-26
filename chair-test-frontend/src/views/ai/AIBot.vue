<template>
  <div class="ai-chat-page">
    <h1>🤖 AI 应用管理</h1>

    <div class="input-area">
      <div class="field">
        <label>用户消息</label>
        <input
          v-model="userMessage"
          placeholder="请输入用户消息"
          @keydown.enter="handleEnterSend"
        />
      </div>
      <div class="btn-group">
        <!--        <button class="btn primary" :disabled="loading" @click="sendStream">-->
        <!--          {{ loading ? "生成中..." : "流式对话" }}-->
        <!--        </button>-->
        <button class="btn" :disabled="loading" @click="sendNormal">
          普通对话
        </button>
        <button class="btn danger" @click="clearOutput">清空输出</button>
      </div>
    </div>

    <div class="output-area" ref="outputWrap">
      <div class="output-header">
        <span>输出结果</span>
        <span v-if="loading" class="cursor-blink">▌</span>
      </div>
      <!-- Markdown 渲染容器 -->
      <div
        class="output-content markdown-body"
        v-html="renderMarkdown(output)"
      ></div>
      <!-- 末尾闪烁光标 -->
      <!-- <span v-if="loading" class="cursor-blink end-cursor">▌</span>-->
    </div>
  </div>
</template>

<script setup>
import { ref, onUnmounted } from "vue";
// 引入 markdown 解析 + 代码高亮
import { marked } from "marked";
import hljs from "highlight.js";
import "highlight.js/styles/atom-one-dark.css";
import { aiBot, aiBotStream } from "@/api/appController";

// 基础配置
const userMessage = ref("你好，你能帮我做什么");
const output = ref("");
const loading = ref(false);
const outputWrap = ref(null);

// 中断控制器
let abortController = null;

// Markdown 配置 + 代码高亮
marked.setOptions({
  highlight: (code, lang) => {
    return hljs.highlightAuto(code, [lang]).value;
  },
  breaks: true, // 回车转为换行
  gfm: true, // 开启 GitHub 风格 Markdown(表格、任务列表等)
});

// 渲染 Markdown
const renderMarkdown = (content) => {
  return marked.parse(content);
};

// 页面卸载终止请求
onUnmounted(() => {
  abortController?.abort();
});

// 自动滚动到底部
const scrollToBottom = () => {
  if (!outputWrap.value) return;
  const content = outputWrap.value.querySelector(".output-content");
  content.scrollTop = content.scrollHeight;
};

// 回车快捷发送
const handleEnterSend = () => {
  if (!loading.value) sendStream();
};

// 清空输出 & 终止请求
const clearOutput = () => {
  abortController?.abort();
  abortController = null;
  output.value = "";
  loading.value = false;
};

// 普通 POST 对话
const sendNormal = async () => {
  if (loading.value) return;
  clearOutput();
  loading.value = true;

  try {
    const res = await aiBot({
      userMessage: userMessage.value,
    });

    output.value = res?.data?.data ?? res?.data ?? "";
    scrollToBottom();
  } catch (err) {
    if (err.name !== "AbortError") {
      output.value += `\n\n❌ 请求出错: ${err.message}`;
      scrollToBottom();
    }
  } finally {
    loading.value = false;
  }
};

// 从 SSE 报文中提取文本内容
const extractTextFromChunk = (data) => {
  let text = "";
  // 优先取 result.output 中的 text 和 reasoningContent
  const output = data?.result?.output;
  if (output) {
    if (output.text) text += output.text;
    if (output.reasoningContent) text += output.reasoningContent;
  }
  // 兜底遍历 results 数组
  if (!text && Array.isArray(data?.results)) {
    for (const item of data.results) {
      const out = item?.output;
      if (out?.text) text += out.text;
      if (out?.reasoningContent) text += out.reasoningContent;
    }
  }
  return text;
};

// 流式 POST 对话（axios 不支持浏览器流式读取，必须用原生 fetch）
const sendStream = async () => {
  if (loading.value) return;
  clearOutput();
  loading.value = true;
  abortController = new AbortController();

  try {
    const res = await aiBotStream(
      {
        userMessage: userMessage.value,
      },
      {
        signal: abortController.signal,
      }
    );

    if (!res.ok) {
      const errorData = await res.json();
      // errorData 格式: { code: xxx, data: null, message: "今日AI调用次数已达上限" }
      console.error(errorData.message);
      alert(errorData.message); // 或用你的 UI 提示方式
      return;
    }

    if (!res.ok) throw new Error(`请求异常 ${res.status}`);
    if (!res.body) throw new Error("当前浏览器不支持流式响应");

    const reader = res.body.getReader();
    const decoder = new TextDecoder("utf-8");
    // 用于拼接可能跨 chunk 的不完整 JSON 行
    let buffer = "";
    let readResult;

    while (!(readResult = await reader.read()).done) {
      buffer += decoder.decode(readResult.value, { stream: true });
      const lines = buffer.split(/\r?\n/);
      // 最后一行可能不完整，留到下次拼接
      buffer = lines.pop() ?? "";

      for (const line of lines) {
        const trimLine = line.trim();
        if (!trimLine || !trimLine.startsWith("data:")) continue;

        const jsonStr = trimLine.slice(5);
        if (!jsonStr) continue;

        try {
          const data = JSON.parse(jsonStr);
          const text = extractTextFromChunk(data);
          if (text) {
            output.value += text;
            scrollToBottom();
          }
        } catch {
          // JSON 解析失败，忽略
        }
      }
    }
  } catch (err) {
    if (err.name !== "AbortError") {
      output.value += `\n\n❌ 流式请求出错: ${err.message}`;
      scrollToBottom();
    }
  } finally {
    loading.value = false;
    abortController = null;
  }
};
</script>

<style scoped>
.ai-chat-page {
  max-width: 860px;
  margin: 0 auto;
  padding: 32px 24px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

h1 {
  font-size: 24px;
  margin: 0 0 24px;
  color: #1f2937;
}

.input-area {
  background: #f9fafb;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
}

.field {
  margin-bottom: 14px;
}

.field label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #4b5563;
  margin-bottom: 6px;
}

.field input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s ease;
  box-sizing: border-box;
  background: #fff;
}

.field input:focus {
  border-color: #4f8cff;
  box-shadow: 0 0 0 2px rgba(79, 140, 255, 0.15);
}

.btn-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 6px;
}

.btn {
  padding: 10px 22px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  background: #e5e7eb;
  color: #1f2937;
}

.btn:hover:not(:disabled) {
  opacity: 0.85;
  transform: translateY(-1px);
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.btn.primary {
  background: #4f8cff;
  color: #fff;
}

.btn.danger {
  background: #ff5c5c;
  color: #fff;
}

.output-area {
  background: #1e1e2e;
  border-radius: 12px;
  overflow: hidden;
  position: relative;
}

.output-header {
  padding: 12px 20px;
  font-size: 13px;
  color: #aaa;
  border-bottom: 1px solid #2a2a3a;
  display: flex;
  align-items: center;
  gap: 6px;
}

/* Markdown 容器基础样式 */
.output-content {
  padding: 20px;
  margin: 0;
  min-height: 200px;
  max-height: 520px;
  overflow-y: auto;
  font-size: 14px;
  line-height: 1.7;
  color: #e0e0e0;
  word-break: break-word;
  scrollbar-width: thin;
  scrollbar-color: #4f8cff #2a2a3a;
}

.output-content::-webkit-scrollbar {
  width: 6px;
}
.output-content::-webkit-scrollbar-track {
  background: #2a2a3a;
}
.output-content::-webkit-scrollbar-thumb {
  background: #4f8cff;
  border-radius: 3px;
}

/* 闪烁光标 */
.cursor-blink {
  animation: blink 1s step-end infinite;
  color: #4f8cff;
  font-weight: bold;
}
.end-cursor {
  position: absolute;
  left: 20px;
  bottom: 20px;
  line-height: 1.7;
}

@keyframes blink {
  50% {
    opacity: 0;
  }
}

/* ========== Markdown 全局样式 ========== */
.markdown-body :deep(h1, h2, h3, h4, h5, h6) {
  margin: 1em 0 0.5em;
  font-weight: 600;
  color: #f0f0f0;
}
.markdown-body :deep(p) {
  margin: 0.8em 0;
}
.markdown-body :deep(ul, ol) {
  padding-left: 1.8em;
  margin: 0.8em 0;
}
.markdown-body :deep(li) {
  margin: 0.3em 0;
}
.markdown-body :deep(blockquote) {
  border-left: 4px solid #4f8cff;
  padding: 0.4em 1em;
  margin: 0.8em 0;
  color: #b0b0b0;
  background: #252535;
}
/* 行内代码 */
.markdown-body :deep(code) {
  background: #2a2a3a;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: "JetBrains Mono", Consolas, monospace;
}
/* 代码块 */
.markdown-body :deep(pre) {
  margin: 1em 0;
  border-radius: 8px;
  overflow: hidden;
}
.markdown-body :deep(pre code) {
  padding: 0;
  background: transparent;
}
.markdown-body :deep(a) {
  color: #4f8cff;
  text-decoration: none;
}
.markdown-body :deep(a:hover) {
  text-decoration: underline;
}
.markdown-body :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 1em 0;
}
.markdown-body :deep(th, td) {
  border: 1px solid #3a3a4a;
  padding: 6px 10px;
}
.markdown-body :deep(th) {
  background: #2a2a3a;
}

/* 移动端适配 */
@media (max-width: 640px) {
  .ai-chat-page {
    padding: 16px 12px;
  }
  .btn-group {
    gap: 8px;
  }
  .btn {
    padding: 8px 16px;
    flex: 1;
  }
}
</style>
