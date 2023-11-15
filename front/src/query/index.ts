import {Attendances, DailyAttendance, FreeLoginUser} from "../types";
import axios, {AxiosResponse} from "axios";

export const downloadAttendancesCsv = (useId: String, year: String, month: String) => {
    window.location.href = `http://localhost:8080/attendances/${useId}/${year}/${month}/download`;
};

export const downloadMessageHistory = (useId: String, year: String, month: String) => {
    window.location.href = `http://localhost:8080/message/${useId}/${year}/${month}/download`;
};

const instance = axios.create({
    baseURL: 'http://localhost:8080'
})

export const fetchAttendances = async (employeeId: string, year: string, month: string): Promise<Attendances> => {
    const {data} = await instance.get<Attendances, AxiosResponse<Attendances>>(`/attendances/${employeeId}/${year}/${month}`)
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
    window.location.href = "https://accounts.secure.freee.co.jp/public_api/select_companies?client_id=b520f9fbdc58ef571fb2caaa92f78270becda1331b53b5cd6dd59712e4aa7259&prompt=select_company&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Ffreee%2Fauthenticate%2Fcallback&response_type=code";
};

export const getTokenFromQueryParameter = (): string | null => {
    const url = new URL(window.location.href);
    return url.searchParams.get("token")
}

export const getFreeeLoginUser = async (code: string): Promise<FreeLoginUser> => {
    const {data} = await instance.get<FreeLoginUser, AxiosResponse<FreeLoginUser>>('/freee/me', {headers: {'code': code}})
    return data
}