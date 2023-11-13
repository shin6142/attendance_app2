import {useState} from "react";
import {postAttendances} from "../query";
import {DailyAttendance} from "../types";

export const RegisterAttendancesButton = (props: RegisterAttendancesButtonProps) => {
    const [value, setValue] = useState("Freeeに勤怠を登録する")
    return (
        <button onClick={()=>{ if(value !== "送信中です"){
            setValue("送信中です")
            postAttendances(props.postData, props.employeeId, props.year, props.month, props.token).then( _  =>
                setValue("成功しました")
            ).catch( _ =>
                setValue("失敗しました")
            )
        }}}>{value}</button>
    )
}

type RegisterAttendancesButtonProps = {
    postData: DailyAttendance[]
    employeeId: string
    year: string
    month: string
    token: string | null
}