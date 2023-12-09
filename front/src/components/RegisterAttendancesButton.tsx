import React, {useState} from "react";
import {postAttendances} from "../query";
import {DailyAttendance} from "../types";

export const RegisterAttendancesButton = (props: RegisterAttendancesButtonProps) => {
    const [value, setValue] = useState("Freeeに勤怠を登録する")
    const [disable, setDisable] = React.useState(false);
    return (
        <button disabled={disable} onClick={() => {
            setValue("登録中です")
            setDisable(true)
            postAttendances(props.postData, props.employeeId, props.year, props.month, props.token).finally(() => {
                setDisable(false)
                setValue("Freeeに勤怠を登録する")
            })
        }}>{value}</button>
    )
}

type RegisterAttendancesButtonProps = {
    postData: DailyAttendance[]
    employeeId: string
    year: string
    month: string
    token: string | null
}