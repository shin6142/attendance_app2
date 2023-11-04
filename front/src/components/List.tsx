import "./scss/list.scss"
import axios, {AxiosResponse} from "axios";
import {useQuery} from 'react-query'
import React, {useState} from "react";


export const List = () => {
    const {isLoading, isError, data} = useAttendances()
    const [datetime, setDatetime] = useState('');
    return (
        <div>
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
                {data?.attendances.map(dailyAttendance =>
                    dailyAttendance.attendances.map((attendance, i) =>
                        <tr key={i}>
                            <td>{attendance.employee_id}</td>
                            <td>{attendance.employee_name}</td>
                            <td>{dailyAttendance.date}</td>
                            <td contentEditable>{attendance.kind}</td>
                            <td><ContentEditable datetime={attendance.datetime} onChange={setDatetime}/></td>
                            <td>{attendance.context}</td>
                        </tr>
                    )
                )}
                </tbody>
            </table>
            <button onClick={getAttendancesTableData}>テーブルの値取得</button>
        </div>
    )
}

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


type Attendance = {
    employee_id: string,
    employee_name: string,
    datetime: string,
    context: string,
    kind: string,
}

type dailyAttendance = {
    date: string,
    attendances: Attendance[]
}

const instance = axios.create({
    baseURL: 'http://localhost:8080'
})

const fetchAttendances = async (): Promise<Attendances> => {
    const {data} = await instance.get<Attendances, AxiosResponse<Attendances>>('/attendances/U02FFCC308G/2023/10')
    return data
}

const postAttendances = async (): Promise<[]> => {
    const {data} = await instance.post<[], AxiosResponse<[]>>('/attendances/1/2023/10/record',
        [{
            date: "2023-10-10",
            attendances: [{
                employee_id: "U02FFCC308G",
                employee_name: "",
                datetime: "2013-02-01T12:52:34",
                context: "",
                kind: "START",
            }, {
                employee_id: "U02FFCC308G",
                employee_name: "",
                datetime: "2013-02-01T12:52:34",
                context: "",
                kind: "LEAVE",
            }, {
                employee_id: "U02FFCC308G",
                employee_name: "",
                datetime: "2013-02-01T12:52:34",
                context: "",
                kind: "BACK",
            }, {
                employee_id: "U02FFCC308G",
                employee_name: "",
                datetime: "2013-02-01T12:52:34",
                context: "",
                kind: "END",
            }]
        }])
    return data
}

export const useAttendances = () => {
    return useQuery({
        queryKey: ['attendances-fetch'],
        queryFn: fetchAttendances,
        staleTime: Infinity,
        cacheTime: Infinity
    })
}

interface ContentEditableProps {
    datetime: string
    onChange: React.Dispatch<React.SetStateAction<string>>
}

const ContentEditable = (props: ContentEditableProps) => {
    const handleInput = (e: { target: { innerHTML: React.SetStateAction<string>; }; }) => {
        props.onChange(e.target.innerHTML);
    };

    return (
        <div
            contentEditable
            onInput={handleInput}
            dangerouslySetInnerHTML={{
                __html: props.datetime,
            }}
        />
    );
};

const getAttendancesTableData = () => {
    type AttendanceTable = HTMLTableElement | null;

    const table: AttendanceTable = document.getElementById('attendances_table');
    const dates: string[][] = [];
    const map = new Map<string, string[]>
    const outer: string[][] = [];
    if (table) {
        for (let row of table.rows) {
            const innerList: string[] = []
            for (let cell of row.cells) {
                console.log(cell.innerText);
                innerList.push(cell.innerText)
            }
            outer.push(innerList)
        }
    } else {
        console.log('Table with id "attendances_table" not found.');
    }
    const dateSet = new Set(dates)
    console.log(outer)

}