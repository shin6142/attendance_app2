import "./scss/list.scss"
import {useEffect, useState} from "react";
import {TableRow} from "./TableRow.tsx";
import {DailyAttendances, DailyAttendance} from "../types";
import {authenticate, fetchAttendances, getFreeeLoginUser, getTokenFromQueryParameter} from "../query";
import {RegisterAttendancesButton} from "./RegisterAttendancesButton.tsx";
import {useForm} from "react-hook-form"


export const Table = () => {
    const [freeeEmployeeId, setFreeeEmployeeId] = useState("")
    const [attendances, setAttendances] = useState<DailyAttendances>()
    const [postData, setPostData] = useState<DailyAttendance[]>([]);

    useEffect(() => {
        setPostData(attendances?.attendances || [])
    }, [attendances]);

    useEffect(() => {
        getFreeeLoginUser(getTokenFromQueryParameter() ?? "").then(result => {
            setFreeeEmployeeId(result.id.toString())
        })
    }, []);

    const year = new Date().getFullYear().toString()
    const month = (new Date().getMonth() + 1).toString()
    const {register, handleSubmit} = useForm<Inputs>({
        defaultValues: {
            employeeId: "",
            channelName: "grp-dev-勤怠",
            year: year,
            month: month
        }
    })
    type Inputs = {
        employeeId: string,
        channelName: string,
        year: string,
        month: string
    }

    return (
        <div>
            <form onSubmit={handleSubmit((inputs) => {
                fetchAttendances(inputs.employeeId, inputs.channelName, inputs.year, inputs.month).then((result) => {
                        setAttendances(result)
                        setPostData(result.attendances)
                    }
                )
            })}>
                <input {...register("employeeId")}/>
                <input {...register("channelName")}/>
                <input {...register("year")}/>
                <input {...register("month")}/>
                <button type={"submit"}>更新</button>
            </form>
            <button onClick={authenticate}>認証</button>
            <RegisterAttendancesButton postData={postData} year={year} month={month} employeeId={freeeEmployeeId}
                                       token={getTokenFromQueryParameter()}></RegisterAttendancesButton>
            <div>
                <p>You are: {attendances?.employee_name}</p>
            </div>
            <table className="table" id={"attendances_table"}>
                <thead>
                <tr>
                    <th>勤務日</th>
                    <th>打刻種別</th>
                    <th>打刻時間</th>
                    <th>メッセージ</th>
                </tr>
                </thead>
                <tbody>
                {attendances?.attendances.map(dailyAttendance =>
                    dailyAttendance.attendances.map((attendance, i) =>
                        <TableRow
                            kind={attendance.kind}
                            key={i}
                            date={dailyAttendance.date}
                            attendance={attendance}
                            setState={(arg) => {
                                console.log(arg)
                            }}
                            parentState={attendances?.attendances ?? []}
                        ></TableRow>
                    )
                )}
                </tbody>
            </table>
        </div>
    )
}
