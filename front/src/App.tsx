import './App.css'
import {Table} from "./components/List";
import {QueryClient, QueryClientProvider} from "react-query";

const queryClient = new QueryClient()
const App = () => {

    const downloadAttendancesCsv = (useId: String, year: String, month: String) => {
        window.location.href = `http://localhost:8080/attendances/${useId}/${year}/${month}/download`;
    };

    const downloadMessageHistory = (useId: String, year: String, month: String) => {
        window.location.href = `http://localhost:8080/message/${useId}/${year}/${month}/download`;
    };

    return (
        <div className="App">
            <QueryClientProvider client={queryClient}>
                <Table/>
                <div>
                    <button type={"button"} onClick={() => downloadAttendancesCsv("U02FFCC308G", "2023", "10")}>
                        アップロード用CSVダウンロード
                    </button>
                    <button type={"button"} onClick={() => downloadMessageHistory("U02FFCC308G", "2023", "10")}>
                        SLACK勤怠報告履歴CSVダウンロード
                    </button>
                </div>
            </QueryClientProvider>

        </div>
    );
}
export default App;
