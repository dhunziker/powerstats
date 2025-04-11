import { api } from 'boot/axios'

// interface RegisterRequest {
//   email: string;
//   password: string;
// }

interface UserLoginRequest {
  email: string;
  password: string;
}

interface UserLoginResponse {
  email: string;
  token: string;
}

// export async function register(email: string, password: string): Promise<UserLoginResponse> {
//   const req: RegisterRequest = { email, password };
//   const response = await api.post<UserLoginResponse>('/api/v1/account/register', req);
//   return response.data;
// }

export async function login(email: string, password: string): Promise<UserLoginResponse> {
  const req: UserLoginRequest = { email, password };
  const response = await api.post<UserLoginResponse>('/api/v1/account/login', req);
  return response.data;
}
