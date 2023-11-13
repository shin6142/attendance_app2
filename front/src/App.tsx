import './App.css'
import {Table} from "./components/Table.tsx";
import {QueryClient, QueryClientProvider} from "react-query";

const queryClient = new QueryClient()
const App = () => {

    return (
        <div className="App">
            <QueryClientProvider client={queryClient}>
                <Table/>
            </QueryClientProvider>
        </div>
    );
}
export default App;
