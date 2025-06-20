import { api } from 'boot/axios';
import type { ApiResponse } from './response';

interface UserRegisterRequest {
  email: string;
  password: string;
}

interface UserActivateRequest {
  activationKey: string;
}

interface UserActivateResponse {
  email: string;
  token: string;
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
  const response = await api.post<void>('/user/register', req);
  return response.data;
}

export async function login(
  email: string,
  password: string,
): Promise<ApiResponse<UserLoginResponse>> {
  const req: UserLoginRequest = { email, password };
  const response = await api.post<ApiResponse<UserLoginResponse>>('/user/login', req);
  return response.data;
}

export async function activate(activationKey: string): Promise<ApiResponse<UserActivateResponse>> {
  const req: UserActivateRequest = { activationKey };
  const response = await api.post<ApiResponse<UserActivateResponse>>('/user/activate', req);
  return response.data;
}
