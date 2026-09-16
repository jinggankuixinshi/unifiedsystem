import request from '../request'

export function getCustomers(params?: any) { return request.get('/sales/customers', params) }
export function createCustomer(data: any) { return request.post('/sales/customers', data) }
export function updateCustomer(id: number, data: any) { return request.put(`/sales/customers/${id}`, data) }
export function deleteCustomer(id: number) { return request.delete(`/sales/customers/${id}`) }

export function getOrders(params?: any) { return request.get('/sales/orders', params) }
export function createOrder(data: any) { return request.post('/sales/orders', data) }
export function getOrderItems(id: number) { return request.get(`/sales/orders/${id}/items`) }

export function getContracts(params?: any) { return request.get('/sales/contracts', params) }

export function getProductPrices(params?: any) { return request.get('/sales/prices', params) }
export function createProductPrice(data: any) { return request.post('/sales/prices', data) }
export function getAnomalyConfigs() { return request.get('/sales/prices/anomaly-config') }
export function updateAnomalyConfig(data: any) { return request.put('/sales/prices/anomaly-config', data) }

export function getAfterSales(params?: any) { return request.get('/sales/aftersales', params) }
export function createAfterSale(data: any) { return request.post('/sales/aftersales', data) }
export function closeAfterSale(id: number) { return request.put(`/sales/aftersales/${id}/close`) }
