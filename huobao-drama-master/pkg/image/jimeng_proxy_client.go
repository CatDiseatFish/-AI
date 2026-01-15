package image

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"time"

	"github.com/drama-generator/backend/pkg/config"
)

// JimengProxyClient 即梦反代客户端
type JimengProxyClient struct {
	baseURL              string
	sessionID            string
	textToImageEndpoint  string
	imageToImageEndpoint string
	connectTimeout       time.Duration
	readTimeout          time.Duration
	model                string
	httpClient           *http.Client
}

// NewJimengProxyClient 创建即梦反代客户端
func NewJimengProxyClient(cfg config.JimengProxyConfig) ImageClient {
	return &JimengProxyClient{
		baseURL:              cfg.BaseURL,
		sessionID:            cfg.SessionID,
		textToImageEndpoint:  cfg.TextToImageEndpoint,
		imageToImageEndpoint: cfg.ImageToImageEndpoint,
		connectTimeout:       time.Duration(cfg.ConnectTimeout) * time.Millisecond,
		readTimeout:          time.Duration(cfg.ReadTimeout) * time.Millisecond,
		model:                cfg.Model,
		httpClient: &http.Client{
			Timeout: time.Duration(cfg.ReadTimeout) * time.Millisecond,
		},
	}
}

// GenerateImage 文生图
func (c *JimengProxyClient) GenerateImage(prompt string, opts ...ImageOption) (*ImageResult, error) {
	// 解析选项
	options := &ImageOptions{}
	for _, opt := range opts {
		opt(options)
	}

	// 构建请求
	reqBody := map[string]interface{}{
		"model":  c.model,
		"prompt": prompt,
	}

	// 添加额外参数
	if options.NegativePrompt != "" {
		reqBody["negative_prompt"] = options.NegativePrompt
	}
	if options.Size != "" {
		reqBody["size"] = options.Size
	}

	reqData, err := json.Marshal(reqBody)
	if err != nil {
		return nil, fmt.Errorf("marshal request: %w", err)
	}

	// 创建请求
	endpoint := c.baseURL + c.textToImageEndpoint
	req, err := http.NewRequest("POST", endpoint, bytes.NewBuffer(reqData))
	if err != nil {
		return nil, fmt.Errorf("create request: %w", err)
	}

	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Cookie", fmt.Sprintf("sessionid=%s", c.sessionID))

	// 发送请求
	resp, err := c.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("send request: %w", err)
	}
	defer resp.Body.Close()

	// 读取响应
	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("read response: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("API error: status=%d, body=%s", resp.StatusCode, string(body))
	}

	// 解析响应
	var result struct {
		Data []struct {
			URL string `json:"url"`
		} `json:"data"`
	}

	if err := json.Unmarshal(body, &result); err != nil {
		return nil, fmt.Errorf("unmarshal response: %w", err)
	}

	if len(result.Data) == 0 {
		return nil, fmt.Errorf("no images in response")
	}

	return &ImageResult{
		ImageURL:  result.Data[0].URL,
		Width:     1024,
		Height:    1024,
		Status:    "success",
		Completed: true,
	}, nil
}

// GetTaskStatus 获取任务状态
func (c *JimengProxyClient) GetTaskStatus(taskID string) (*ImageResult, error) {
	// 即梦反代直接返回结果，没有异步任务
	return &ImageResult{
		TaskID:    taskID,
		Status:    "completed",
		Completed: true,
	}, nil
}
