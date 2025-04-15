import { api } from 'boot/axios';

export interface ApiKey {
  id: number;
  accountId: number;
  key: string;
  creationDate: Date;
  expiryDate: Date;
}

export async function getApiKeys(): Promise<ApiKey[]> {
  const response = await api.get<ApiKey[]>('/api-key');
  return response.data;
}
