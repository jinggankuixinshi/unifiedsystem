import request from '../request'

export function getTypes() { return request.get('/system/dict/types') }
export function saveType(data: any) { return request.post('/system/dict/types', data) }
export function updateType(id: number, data: any) { return request.put(`/system/dict/types/${id}`, data) }
export function deleteType(id: number) { return request.delete(`/system/dict/types/${id}`) }

export function getDataByType(type: string) { return request.get('/system/dict/data', { type }) }
export function saveData(data: any) { return request.post('/system/dict/data', data) }
export function updateData(id: number, data: any) { return request.put(`/system/dict/data/${id}`, data) }
export function deleteData(id: number) { return request.delete(`/system/dict/data/${id}`) }

export function getAuditLogs(params?: any) { return request.get('/system/audit-logs', params) }
export function getAttendanceRecords(params?: any) { return request.get('/system/attendance/records', params) }
export function clockIn() { return request.post('/system/attendance/clock-in') }
export function clockOut() { return request.post('/system/attendance/clock-out') }
export function applyLeave(data: any) { return request.post('/system/attendance/leave', data) }
export function getLeaves(params?: any) { return request.get('/system/attendance/leave', params) }
export function applyOvertime(data: any) { return request.post('/system/attendance/overtime', data) }
export function getOvertimes(params?: any) { return request.get('/system/attendance/overtime', params) }
