// Asset Status
export enum AssetStatus {
  NONE = 'NONE',
  GENERATING = 'GENERATING',
  READY = 'READY',
  FAILED = 'FAILED',
}

// Job Status
export enum JobStatus {
  PENDING = 'PENDING',
  GENERATING = 'GENERATING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
}

// Job Type
export enum JobType {
  GENERATE_IMAGE = 'GENERATE_IMAGE',
  GENERATE_VIDEO = 'GENERATE_VIDEO',
  PARSE_TEXT = 'PARSE_TEXT',
  EXPORT = 'EXPORT',
}

// Toolbox Generation Type
export enum ToolboxType {
  TEXT = 'TEXT',
  IMAGE = 'IMAGE',
  VIDEO = 'VIDEO',
}

// Generation Mode
export enum GenerateMode {
  ALL = 'ALL',
  MISSING = 'MISSING',
}
