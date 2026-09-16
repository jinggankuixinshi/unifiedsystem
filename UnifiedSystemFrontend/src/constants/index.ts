export const ErrorCodes = {
  SUCCESS: 200,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  INTERNAL_ERROR: 500,

  INVALID_TOKEN: 1001,
  KICKED_OUT: 1002,
  ACCOUNT_DISABLED: 1003,

  USER_NOT_FOUND: 2001,
  USERNAME_EXISTS: 2002,
  ROLE_NOT_FOUND: 2005,
  DEPT_NOT_FOUND: 2007,
  DEPT_HAS_CHILDREN: 2008,
  RESOURCE_NOT_FOUND: 2009,

  DUPLICATE_SUBMIT: 3001,
  STOCK_INSUFFICIENT: 3002,
  ORDER_NOT_FOUND: 4001,
  ORDER_STATUS_ERROR: 4002,
  APPROVAL_NOT_FOUND: 4005,
  APPROVAL_ALREADY: 4008,

  TRANSFER_CHECK_FAIL: 5001,
  QUALITY_CHECK_FAIL: 5002,
  CONTRACT_SIGN_FAIL: 5003,
} as const

export const OrderStatus = {
  PENDING: 0,
  IN_PROGRESS: 1,
  COMPLETED: 2,
  CANCELLED: 3,
} as const

export const ApprovalStatus = {
  PENDING: 0,
  APPROVED: 1,
  REJECTED: 2,
} as const

export const PriceAnomalyLevel = {
  NORMAL: 0,
  MILD: 1,
  MODERATE: 2,
  SEVERE: 3,
} as const

export const CacheKeys = {
  USER_INFO: 'user:info',
  DICT_DATA: 'dict:data',
  AQL_CONFIG: 'production:aql:config',
} as const

export const StatusLabels: Record<number, string> = {
  [ApprovalStatus.PENDING]: '待审批',
  [ApprovalStatus.APPROVED]: '已通过',
  [ApprovalStatus.REJECTED]: '已驳回',
}

export const StatusColors: Record<number, string> = {
  [ApprovalStatus.PENDING]: 'orange',
  [ApprovalStatus.APPROVED]: 'green',
  [ApprovalStatus.REJECTED]: 'red',
}
