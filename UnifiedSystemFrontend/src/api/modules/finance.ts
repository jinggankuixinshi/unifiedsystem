import request from '../request'

export function getAccounts(params?: any) { return request.get('/finance/accounts', { params }) }
export function getSubjects(params?: any) { return request.get('/finance/subjects', { params }) }

export function getVouchers(params?: any) { return request.get('/finance/vouchers', { params }) }
export function createVoucher(summary: string, entries: any[]) { return request.post(`/finance/vouchers?summary=${summary}`, entries) }

export function getExpenses(params?: any) { return request.get('/finance/expenses', { params }) }
export function createExpense(data: any) { return request.post('/finance/expenses', data) }

export function getSalaries(params?: any) { return request.get('/finance/salaries', { params }) }
export function createSalary(data: any) { return request.post('/finance/salaries', data) }
export function updateSalary(id: number, data: any) { return request.put(`/finance/salaries/${id}`, data) }

export function getReceivables(params?: any) { return request.get('/finance/receivables', { params }) }
export function createReceivable(data: any) { return request.post('/finance/receivables', data) }
export function logReceivablePayment(id: number, amount: number, method: string) { return request.post(`/finance/receivables/${id}/payment`, { amount, paymentMethod: method }) }

export function getPayables(params?: any) { return request.get('/finance/payables', { params }) }
export function createPayable(data: any) { return request.post('/finance/payables', data) }
export function logPayablePayment(id: number, amount: number, method: string) { return request.post(`/finance/payables/${id}/payment`, { amount, paymentMethod: method }) }

export function getBudgets(params?: any) { return request.get('/finance/budgets', { params }) }
export function createBudget(data: any) { return request.post('/finance/budgets', data) }
export function updateBudget(id: number, data: any) { return request.put(`/finance/budgets/${id}`, data) }
