import "./scss/list.scss"
import axios, {AxiosResponse} from "axios";
import React, {useEffect, useState} from "react";
import TextField from "@material-ui/core/TextField";


export const Table = () => {
    const [employeeId, setEmployeeId] = useState("U02FFCC308G")
    const [year, setYear] = useState(new Date().getFullYear().toString())
    const [month, setMonth] = useState((new Date().getMonth() + 1).toString())
    const [attendances, setAttendances] = useState<Attendances>()
    // const {data: attendances} = useAttendances(employeeId, year, month)
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
    }, []); // 空の依存リストを渡して、コンポーネントがマウントされた時にのみ実行されるようにします

    return (
        <div>
            <input type={"text"} value={employeeId} onChange={(event) => {
                setEmployeeId(event.target.value)
            }}/>
            <input type={"number"} value={year} onChange={(event) => {
                setYear(event.target.value)
            }}/>
            <input type={"number"} value={month} onChange={(event) => {
                setMonth(event.target.value)
            }}/>
            <button onClick={updateAttendances}>テーブルを表示</button>
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
                                  setState={setPostData}
                                  parentState={attendances.attendances}
                        ></TableRow>
                    )
                )}
                </tbody>
            </table>
            <button onClick={() => {
                postAttendances(postData)
            }}>Freeeに登録</button>
            <button onClick={() => {
                console.log(postData)
            }}>attendances
            </button>
        </div>
    )
}

type DateTimeInputProps = {
    date: string
    attendance: AttendanceToRequest
    parentState: DailyAttendance[]
    setState: React.Dispatch<React.SetStateAction<DailyAttendance[]>>
}

const TableRow = (props: DateTimeInputProps) => {
    const [datetime, setDatetime] = useState<string>(props.attendance.datetime)
    useEffect(() => {
        setDatetime(props.attendance.datetime)
    }, [props.parentState])
    const updateDateTime = () => {
        const updated = props.parentState.map((dailyAttendance) => {
            if (dailyAttendance.date == props.date) {
                dailyAttendance.attendances.map((attendance) => {
                    if (attendance.kind == props.attendance.kind) {
                        attendance.datetime = datetime
                    }
                })
            }
            return dailyAttendance
        })
        props.setState(updated)
        console.log(updated)
    }
    return (
        <tr>
            <td>{props.attendance.employee_id}</td>
            <td>{props.attendance.employee_name}</td>
            <td>{props.date}</td>
            <td>{props.attendance.kind}</td>
            <td>
                <TextField value={datetime} onChange={(event) => {
                    setDatetime(event.target.value)
                }}/>
                <button onClick={updateDateTime}>更新</button>
            </td>
            <td>{props.attendance.context}</td>
        </tr>
    );
}

const instance = axios.create({
    baseURL: 'http://localhost:8080'
})

const fetchAttendances = async (employeeId: string, year: string, month: string): Promise<Attendances> => {
    const {data} = await instance.get<Attendances, AxiosResponse<Attendances>>(`/attendances/${employeeId}/${year}/${month}`)
    return data
}

const postAttendances = async (attendances: DailyAttendance[]): Promise<[]> => {
    const {data} = await instance.post<[], AxiosResponse<[]>>('/attendances/1164735/2023/11/record', attendances)
    return data
}

// const useAttendances = (employeeId: string, year: string, month: string) => {
//     return useQuery({
//         queryKey: ['attendances-fetch'],
//         queryFn: async (): Promise<Attendances> => {
//             const {data} = await instance.get<Attendances, AxiosResponse<Attendances>>(`/attendances/${employeeId}/${year}/${month}`)
//             return data
//         },
//         staleTime: Infinity,
//         cacheTime: Infinity
//     })
// }


type Attendances = {
    attendances: DailyAttendance[]
}

type DailyAttendance = {
    date: string,
    attendances: AttendanceToRequest[]
}

type AttendanceToRequest = {
    employee_id: number,
    employee_name: string,
    datetime: string,
    context: string,
    kind: string,
}