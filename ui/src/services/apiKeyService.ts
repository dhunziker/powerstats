import { api } from 'boot/axios';

export interface ApiKey {
  id: number;
  accountId: number;
  name: string
  keyHash: Int8Array;
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

export async function getApiKeys(): Promise<ApiKey[]> {
  const response = await api.get<ApiKey[]>('/api-key');
  return response.data;
}

export async function createApiKey(name: string): Promise<ApiKeyCreateResponse> {
  const req: ApiKeyCreateRequest = { name };
  const response = await api.post<ApiKeyCreateResponse>('/api-key', req);
  return response.data;
}

export async function deleteApiKey(id: number): Promise<void> {
  const response = await api.delete<void>(`/api-key/${id}`);
  return response.data;
}
