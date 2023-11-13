import "./scss/list.scss"
import {useEffect, useState} from "react";
import {TableRow} from "./TableRow.tsx";
import {Attendances, DailyAttendance} from "../types";
import {authenticate, fetchAttendances, getTokenFromQueryParameter} from "../query";
import {RegisterAttendancesButton} from "./RegisterAttendancesButton.tsx";


export const Table = () => {
    const [employeeId, setEmployeeId] = useState("U02FFCC308G")
    const [freeeEmployeeId, setFreeeEmployeeId] = useState("1164735")
    const [year, setYear] = useState(new Date().getFullYear().toString())
    const [month, setMonth] = useState((new Date().getMonth() + 1).toString())
    const [attendances, setAttendances] = useState<Attendances>()
    const [postData, setPostData] = useState<DailyAttendance[]>([]);
    const updateAttendances = () => {
        fetchAttendances(employeeId, year, month).then(result => {
                setAttendances(result)
                setPostData(result.attendances)
            }
        )
    }

    useEffect(() => {
        fetchAttendances(employeeId, year, month).then(result => {
                setAttendances(result)
                setPostData(result.attendances)
            }
        )
    }, []);

    return (
        <div>
            <button onClick={authenticate}>認証</button>
            <input type={"text"} value={employeeId} onChange={(event) => {
                setEmployeeId(event.target.value)
            }}/>
            <input type={"number"} value={year} onChange={(event) => {
                setYear(event.target.value)
            }}/>
            <input type={"number"} value={month} onChange={(event) => {
                setMonth(event.target.value)
            }}/>
            <input type={"number"} value={freeeEmployeeId} onChange={(event) => {
                setFreeeEmployeeId(event.target.value)
            }}/>
            <button onClick={updateAttendances}>更新</button>
            <RegisterAttendancesButton postData={postData} employeeId={freeeEmployeeId} year={year} month={month} token={getTokenFromQueryParameter()}></RegisterAttendancesButton>
            <table className="table" id={"attendances_table"}>
                <thead>
                <tr>
                    <th>従業員ID</th>
                    <th>従業員名</th>
                    <th>勤務日</th>
                    <th>打刻種別</th>
                    <th>打刻時間</th>
                    <th>メッセージ</th>
                </tr>
                </thead>
                <tbody>
                {attendances?.attendances.map(dailyAttendance =>
                    dailyAttendance.attendances.map((attendance, i) =>
                        <TableRow key={i}
                                  date={dailyAttendance.date}
                                  attendance={attendance}
                                  setState={(arg) => {
                                      console.log(arg)
                                  }}
                                  parentState={attendances.attendances}
                        ></TableRow>
                    )
                )}
                </tbody>
            </table>
        </div>
    )
}
