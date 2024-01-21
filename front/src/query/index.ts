import {DailyAttendances, DailyAttendance, FreeLoginUser} from "../types";
import axios, {AxiosResponse} from "axios";

export const instance = axios.create({
    baseURL: import.meta.env.VITE_MANAGER_API_URL ?? 'http://localhost:8080'
})

export const fetchAttendances = async (employeeId: string, channelName: string, year: string, month: string): Promise<DailyAttendances> => {
    const {data} = await instance.get<DailyAttendances, AxiosResponse<DailyAttendances>>(`/attendances/${employeeId}/${channelName}/${year}/${month}`)
    return data
}

export const postAttendances = async (attendances: DailyAttendance[], employeeId: string, year: string, month: string, token: string | null = ""): Promise<[]> => {
    const headers = {
        code: token
    }
    const {data} = await instance.post<[], AxiosResponse<[]>>(`/attendances/${employeeId}/${year}/${month}/record`, attendances, {headers: headers})
    return data
}

export const authenticate = () => {
    const clientId = import.meta.env.VITE_FREEE_CLIENT_ID
    window.location.href = `https://accounts.secure.freee.co.jp/public_api/select_companies?client_id=${clientId}&prompt=select_company&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Ffreee%2Fauthenticate%2Fcallback&response_type=code`;
};

export const getTokenFromQueryParameter = (): string | null => {
    const url = new URL(window.location.href);
    return url.searchParams.get("token")
}

export const getFreeeLoginUser = async (code: string): Promise<FreeLoginUser> => {
    const {data} = await instance.get<FreeLoginUser, AxiosResponse<FreeLoginUser>>('/freee/me', {headers: {'code': code}})
    return data
}