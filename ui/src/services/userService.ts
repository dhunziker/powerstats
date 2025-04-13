import { api } from 'boot/axios'

interface UserRegisterRequest {
  email: string;
  password: string;
}

interface UserLoginRequest {
  email: string;
  password: string;
}

interface UserLoginResponse {
  email: string;
  token: string;
}

export async function register(email: string, password: string): Promise<void> {
  const req: UserRegisterRequest = { email, password };
  const response = await api.post<void>('/api/v1/user/register', req);
  return response.data;
}

export async function login(email: string, password: string): Promise<UserLoginResponse> {
  const req: UserLoginRequest = { email, password };
  const response = await api.post<UserLoginResponse>('/api/v1/user/login', req);
  return response.data;
}
