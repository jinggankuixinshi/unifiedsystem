import request from '../request'

export function getProducts(params?: any) { return request.get('/production/products', { params }) }
export function getProductById(id: number) { return request.get(`/production/products/${id}`) }
export function createProduct(data: any) { return request.post('/production/products', data) }
export function updateProduct(id: number, data: any) { return request.put(`/production/products/${id}`, data) }
export function deleteProduct(id: number) { return request.delete(`/production/products/${id}`) }

export function getMaterials(params?: any) { return request.get('/production/materials', { params }) }
export function createMaterial(data: any) { return request.post('/production/materials', data) }
export function updateMaterial(id: number, data: any) { return request.put(`/production/materials/${id}`, data) }
export function deleteMaterial(id: number) { return request.delete(`/production/materials/${id}`) }

export function getBomByProduct(productId: number) { return request.get(`/production/bom/product/${productId}`) }
export function getBomItems(bomId: number) { return request.get(`/production/bom/${bomId}/items`) }
