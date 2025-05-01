import { api } from 'boot/axios';

interface UserRegisterRequest {
  email: string;
  password: string;
}

interface UserActivateRequest {
  activationKey: string;
}

interface ApiResponse {
  status: string;
  statusCode: number;
}

interface UserActivateResponse extends ApiResponse {
  data: {
    email: string;
    token: string;
  }
}

interface UserLoginRequest {
  email: string;
  password: string;
}

interface UserLoginResponse extends ApiResponse {
  data: {
    email: string;
    token: string;
  }
}

export async function register(email: string, password: string): Promise<void> {
  const req: UserRegisterRequest = { email, password };
  const response = await api.post<void>('/user/register', req);
  return response.data;
}

export async function login(email: string, password: string): Promise<UserLoginResponse> {
  const req: UserLoginRequest = { email, password };
  const response = await api.post<UserLoginResponse>('/user/login', req);
  return response.data;
}

export async function activate(activationKey: string): Promise<UserActivateResponse> {
  const req: UserActivateRequest = { activationKey };
  const response = await api.post<UserActivateResponse>('/user/activate', req);
  return response.data;
}
