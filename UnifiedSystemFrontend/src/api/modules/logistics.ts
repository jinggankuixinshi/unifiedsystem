import request from '../request'

export function getStockList(params?: any) { return request.get('/logistics/warehouse', { params }) }
export function createInbound(data: any) { return request.post('/logistics/warehouse/inbound', data) }
export function createOutbound(data: any) { return request.post('/logistics/warehouse/outbound', data) }
export function createQualityCheck(data: any) { return request.post('/logistics/warehouse/quality-check', data) }

export function getTransfers(params?: any) { return request.get('/logistics/transfers', { params }) }
export function createTransfer(data: any) { return request.post('/logistics/transfers', data) }
export function signTransfer(id: number) { return request.post(`/logistics/transfers/${id}/sign`) }

export function getShippings(params?: any) { return request.get('/logistics/shippings', { params }) }
export function createShipping(data: any) { return request.post('/logistics/shippings', data) }
export function markPrinted(id: number) { return request.put(`/logistics/shippings/${id}/print`) }

export function createPicking(shippingId: number) { return request.post('/logistics/pickings', null, { params: { shippingId } }) }
