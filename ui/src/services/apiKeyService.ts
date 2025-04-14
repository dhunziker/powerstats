import { api } from 'boot/axios'

interface ApiKeyResponse {
  id: number,
  accountId: number,
  key: string,
  creationDate: Date,
  expiryDate: Date
}

export async function getApiKeys(): Promise<ApiKeyResponse[]> {
  const response = await api.get<ApiKeyResponse[]>('/api-key');
  return response.data;
}
