import {useState} from "react";

export const MonthYear = () => {
    const [year, setYear] = useState(new Date().getFullYear());
    const [month, setMonth] = useState((new Date().getMonth() + 1));
    return (
        <>
            <h1>{year}/{month}</h1>
            <button onClick={() => {
                const date = new Date(year, month - 2)
                setYear(date.getFullYear())
                setMonth((date.getMonth() + 1))
            }}>前月</button>
            <button onClick={() => {
                const date = new Date(year, month + 2)
                setYear(date.getFullYear())
                setMonth((date.getMonth() + 1))
            }}>次月</button>
        </>
    );
}