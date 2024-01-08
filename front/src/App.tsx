import './App.css'
import {AttendancesTable} from "./components/Attendances/AttendancesTable.tsx";
import {QueryClient, QueryClientProvider} from "react-query";
import {ReactQueryDevtools} from "react-query/devtools";

const queryClient = new QueryClient()
const App = () => {

    return (
        <div className="App">
            <QueryClientProvider client={queryClient}>
                <AttendancesTable/>
                <ReactQueryDevtools initialIsOpen={false}/>
            </QueryClientProvider>
        </div>
    );
}
export default App;
