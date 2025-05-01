import { api } from 'boot/axios';

export interface ApiKey {
  id: number;
  accountId: number;
  name: string
  keyHash: Int8Array;
  creationDate: Date;
  expiryDate: Date;
}

interface ApiResponse {
  status: string;
  statusCode: number;
}

interface ApiKeyResponse extends ApiResponse {
  data: ApiKey[];
}

interface ApiKeyCreateRequest {
  name: string;
}

interface ApiKeyCreateResponse extends ApiResponse {
  data: {
    key: string;
    apiKey: ApiKey;
  }
}

export async function getApiKeys(): Promise<ApiKeyResponse> {
  const response = await api.get<ApiKeyResponse>('/api/v1/api-key');
  return response.data;
}

export async function createApiKey(name: string): Promise<ApiKeyCreateResponse> {
  const req: ApiKeyCreateRequest = { name };
  const response = await api.post<ApiKeyCreateResponse>('/api/v1/api-key', req);
  return response.data;
}

export async function deleteApiKey(id: number): Promise<void> {
  const response = await api.delete<void>(`/api/v1/api-key/${id}`);
  return response.data;
}
