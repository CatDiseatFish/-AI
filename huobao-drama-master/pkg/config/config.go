package config

import (
	"fmt"

	"github.com/spf13/viper"
)

type Config struct {
	App      AppConfig      `mapstructure:"app"`
	Server   ServerConfig   `mapstructure:"server"`
	Database DatabaseConfig `mapstructure:"database"`
	Storage  StorageConfig  `mapstructure:"storage"`
	AI       AIConfig       `mapstructure:"ai"`
}

type AppConfig struct {
	Name    string `mapstructure:"name"`
	Version string `mapstructure:"version"`
	Debug   bool   `mapstructure:"debug"`
}

type ServerConfig struct {
	Port         int      `mapstructure:"port"`
	Host         string   `mapstructure:"host"`
	CORSOrigins  []string `mapstructure:"cors_origins"`
	ReadTimeout  int      `mapstructure:"read_timeout"`
	WriteTimeout int      `mapstructure:"write_timeout"`
}

type DatabaseConfig struct {
	Type     string `mapstructure:"type"` // sqlite, mysql
	Path     string `mapstructure:"path"` // SQLite数据库文件路径
	Host     string `mapstructure:"host"`
	Port     int    `mapstructure:"port"`
	User     string `mapstructure:"user"`
	Password string `mapstructure:"password"`
	Database string `mapstructure:"database"`
	Charset  string `mapstructure:"charset"`
	MaxIdle  int    `mapstructure:"max_idle"`
	MaxOpen  int    `mapstructure:"max_open"`
}

type StorageConfig struct {
	Type      string `mapstructure:"type"`       // local, minio
	LocalPath string `mapstructure:"local_path"` // 本地存储路径
	BaseURL   string `mapstructure:"base_url"`   // 访问URL前缀
}

type AIConfig struct {
	DefaultTextProvider  string              `mapstructure:"default_text_provider"`
	DefaultImageProvider string              `mapstructure:"default_image_provider"`
	DefaultVideoProvider string              `mapstructure:"default_video_provider"`
	VectorEngine         VectorEngineConfig  `mapstructure:"vectorengine"`
	Text                 TextConfig          `mapstructure:"text"`
	Image                ImageConfig         `mapstructure:"image"`
	Video                VideoConfig         `mapstructure:"video"`
	Doubao               DoubaoConfig        `mapstructure:"doubao"`
}

// VectorEngineConfig 向量引擎中转站配置
type VectorEngineConfig struct {
	Enabled        bool   `mapstructure:"enabled"`
	BaseURL        string `mapstructure:"base_url"`
	APIKey         string `mapstructure:"api_key"`
	Timeout        int    `mapstructure:"timeout"`
	ConnectTimeout int    `mapstructure:"connect_timeout"`
	ReadTimeout    int    `mapstructure:"read_timeout"`
}

// TextConfig 文本生成配置
type TextConfig struct {
	Model       string  `mapstructure:"model"`
	MaxTokens   int     `mapstructure:"max_tokens"`
	Temperature float64 `mapstructure:"temperature"`
	TopP        float64 `mapstructure:"top_p"`
}

// ImageConfig 图片生成配置
type ImageConfig struct {
	DefaultModel        string            `mapstructure:"default_model"`
	DefaultAspectRatio  string            `mapstructure:"default_aspect_ratio"`
	DefaultCount        int               `mapstructure:"default_count"`
	JimengProxy         JimengProxyConfig `mapstructure:"jimeng_proxy"`
}

// JimengProxyConfig 即梦反代配置
type JimengProxyConfig struct {
	Enabled               bool   `mapstructure:"enabled"`
	BaseURL               string `mapstructure:"base_url"`
	SessionID             string `mapstructure:"sessionid"`
	TextToImageEndpoint   string `mapstructure:"text_to_image_endpoint"`
	ImageToImageEndpoint  string `mapstructure:"image_to_image_endpoint"`
	ConnectTimeout        int    `mapstructure:"connect_timeout"`
	ReadTimeout           int    `mapstructure:"read_timeout"`
	Model                 string `mapstructure:"model"`
}

// VideoConfig 视频生成配置
type VideoConfig struct {
	Model              string `mapstructure:"model"`
	DefaultAspectRatio string `mapstructure:"default_aspect_ratio"`
	DefaultDuration    int    `mapstructure:"default_duration"`
	MaxDuration        int    `mapstructure:"max_duration"`
	PollInterval       int    `mapstructure:"poll_interval"`
	MaxPollCount       int    `mapstructure:"max_poll_count"`
}

// DoubaoConfig 豆包视频生成配置
type DoubaoConfig struct {
	Enabled       bool   `mapstructure:"enabled"`
	BaseURL       string `mapstructure:"base_url"`
	APIKey        string `mapstructure:"api_key"`
	Model         string `mapstructure:"model"`
	Endpoint      string `mapstructure:"endpoint"`
	QueryEndpoint string `mapstructure:"query_endpoint"`
}

func LoadConfig() (*Config, error) {
	viper.SetConfigName("config")
	viper.SetConfigType("yaml")
	viper.AddConfigPath("./configs")
	viper.AddConfigPath(".")

	viper.AutomaticEnv()

	if err := viper.ReadInConfig(); err != nil {
		return nil, fmt.Errorf("failed to read config: %w", err)
	}

	var config Config
	if err := viper.Unmarshal(&config); err != nil {
		return nil, fmt.Errorf("failed to unmarshal config: %w", err)
	}

	return &config, nil
}

func (c *DatabaseConfig) DSN() string {
	if c.Type == "sqlite" {
		return c.Path
	}
	// MySQL DSN
	return fmt.Sprintf("%s:%s@tcp(%s:%d)/%s?charset=%s&parseTime=True&loc=Local",
		c.User,
		c.Password,
		c.Host,
		c.Port,
		c.Database,
		c.Charset,
	)
}
