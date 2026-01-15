package ai

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"time"

	"github.com/drama-generator/backend/pkg/config"
)

// VectorEngineClient 向量引擎中转站客户端
type VectorEngineClient struct {
	baseURL        string
	apiKey         string
	timeout        time.Duration
	connectTimeout time.Duration
	readTimeout    time.Duration
	model          string
	httpClient     *http.Client
}

// NewVectorEngineClient 创建向量引擎客户端
func NewVectorEngineClient(cfg config.VectorEngineConfig, model string) AIClient {
	return &VectorEngineClient{
		baseURL:        cfg.BaseURL,
		apiKey:         cfg.APIKey,
		timeout:        time.Duration(cfg.Timeout) * time.Millisecond,
		connectTimeout: time.Duration(cfg.ConnectTimeout) * time.Millisecond,
		readTimeout:    time.Duration(cfg.ReadTimeout) * time.Millisecond,
		model:          model,
		httpClient: &http.Client{
			Timeout: time.Duration(cfg.Timeout) * time.Millisecond,
		},
	}
}

// GenerateText 实现文本生成接口
func (c *VectorEngineClient) GenerateText(prompt string, systemPrompt string, options ...func(*ChatCompletionRequest)) (string, error) {
	// 构建消息
	messages := []map[string]string{
		{
			"role":    "system",
			"content": systemPrompt,
		},
		{
			"role":    "user",
			"content": prompt,
		},
	}

	// 构建请求
	reqBody := map[string]interface{}{
		"model":    c.model,
		"messages": messages,
	}

	reqData, err := json.Marshal(reqBody)
	if err != nil {
		return "", fmt.Errorf("marshal request: %w", err)
	}

	// 创建请求
	req, err := http.NewRequest("POST", c.baseURL+"/v1/chat/completions", bytes.NewBuffer(reqData))
	if err != nil {
		return "", fmt.Errorf("create request: %w", err)
	}

	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", "Bearer "+c.apiKey)

	// 发送请求
	resp, err := c.httpClient.Do(req)
	if err != nil {
		return "", fmt.Errorf("send request: %w", err)
	}
	defer resp.Body.Close()

	// 读取响应
	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return "", fmt.Errorf("read response: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return "", fmt.Errorf("API error: status=%d, body=%s", resp.StatusCode, string(body))
	}

	// 解析响应
	var result struct {
		Choices []struct {
			Message struct {
				Content string `json:"content"`
			} `json:"message"`
		} `json:"choices"`
	}

	if err := json.Unmarshal(body, &result); err != nil {
		return "", fmt.Errorf("unmarshal response: %w", err)
	}

	if len(result.Choices) == 0 {
		return "", fmt.Errorf("no choices in response")
	}

	return result.Choices[0].Message.Content, nil
}

// TestConnection 测试连接
func (c *VectorEngineClient) TestConnection() error {
	_, err := c.GenerateText("Hello", "You are a helpful assistant.")  
	return err
}
