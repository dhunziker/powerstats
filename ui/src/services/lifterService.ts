import { api } from 'boot/axios';
import type { ApiResponse } from './response';

export interface PersonalBest {
  name: string;
  sex: string;
  equipment: string;
  best3SquatKg?: number;
  best3BenchKg?: number;
  best3DeadliftKg?: number;
  totalKg?: number;
  dots?: number;
  wilks?: number;
  glossbrenner?: number;
  goodlift?: number;
}

export interface Event {
  name: string;
  date: string;
  location: string;
  federation: string;
}

export async function findLifters(namePattern: string): Promise<ApiResponse<string[]>> {
  const response = await api.get<ApiResponse<string[]>>(`/v1/lifter`, {
    params: {
      namePattern: namePattern,
    },
  });
  return response.data;
}

export async function findPersonalBests(name: string): Promise<ApiResponse<PersonalBest>> {
  const response = await api.get<ApiResponse<PersonalBest>>(`/v1/lifter/${name}/personal-bests`);
  return response.data;
}

export async function findEvents(name: string): Promise<ApiResponse<Event>> {
  const response = await api.get<ApiResponse<Event>>(`/v1/lifter/${name}/events`);
  return response.data;
}
