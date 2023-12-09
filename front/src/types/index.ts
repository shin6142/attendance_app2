export type DailyAttendances = {
    attendances: DailyAttendance[]
}

export type DailyAttendance = {
    date: string,
    attendances: Attendance[]
}

export type Attendance = {
    attendance_id: string,
    employee_id: number,
    employee_name: string,
    datetime: string,
    context: string,
    kind: string,
}

export type FreeLoginUser = {
    id: number,
    name: string,
    company_name: string,
    company_id: number,
}