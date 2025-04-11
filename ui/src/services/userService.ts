import { api } from 'boot/axios'

interface RegisterRequest {
  email: string;
  password: string;
}

interface AuthRequest {
  email: string;
  password: string;
}

interface AuthResponse {
  id: string;
  email: string;
  password_hash: string;
}

export async function register(email: string, password: string): Promise<AuthResponse> {
  const req: RegisterRequest = { email, password };
  const response = await api.post<AuthResponse>('/api/v1/account/register', req);
  return response.data;
}

export async function login(email: string, password: string): Promise<AuthResponse> {
  const req: AuthRequest = { email, password };
  const response = await api.post<AuthResponse>('/api/v1/account/login', req);
  return response.data;
}
