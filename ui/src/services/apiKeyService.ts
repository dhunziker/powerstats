import { api } from 'boot/axios';
import type { ApiResponse } from './response';

export interface ApiKey {
  id: number;
  accountId: number;
  name: string;
  publicKey: string;
  secretKeyHash: Int8Array;
  creationDate: Date;
  expiryDate: Date;
}

interface ApiKeyCreateRequest {
  name: string;
}

interface ApiKeyCreateResponse {
  key: string;
  apiKey: ApiKey;
}

export async function getApiKeys(): Promise<ApiResponse<ApiKey[]>> {
  const response = await api.get<ApiResponse<ApiKey[]>>('/v1/api-key');
  return response.data;
}

export async function createApiKey(name: string): Promise<ApiResponse<ApiKeyCreateResponse>> {
  const req: ApiKeyCreateRequest = { name };
  const response = await api.post<ApiResponse<ApiKeyCreateResponse>>('/v1/api-key', req);
  return response.data;
}

export async function deleteApiKey(id: number): Promise<void> {
  const response = await api.delete<void>(`/v1/api-key/${id}`);
  return response.data;
}
