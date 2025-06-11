import './App.css';
import {
    BrowserRouter as Router,
    Routes,
    Route,
    Navigate
} from 'react-router-dom';
import DefaultLayout from './pages/component/default-layout';
import Login from'./pages/component/auth/login';
import SignUp from'./pages/component/auth/signUp';
import Calendar from './pages/component/vacations/calendar';
import Vacations from "./pages/component/vacations/vacations";
import FirstApprovalList from './pages/component/approval/FirstApprovalList'
import SecondApprovalList from './pages/component/approval/SecondApprovalList'
import FirstApprovalDetail from './pages/component/approval/FirstApprovalDetail'
import SecondApprovalDetail from './pages/component/approval/SecondApprovalDetail'
import MemberApprovalList from './pages/component/admin/MemberApprovalList'
import VacationList from './pages/admin/vacation-list'
import CodeManagement from "./pages/admin/code-management";
import VacationDetail from "./pages/admin/vacation-detail";
import MyVacationStatus from './pages/component/vacations/MyVacationStatus';
import VacationRequest from './pages/component/vacations/VacationRequest';
import VacationHistory from './pages/component/vacations/VacationHistory';
import VacationEdit from './pages/component/vacations/VacationEdit';
import VacationManagerPage from './pages/component/vacations/statistics';

function App() {
    return (
        <Router>
            <DefaultLayout>
                <Routes>
                    <Route path="/" element={<Navigate to="/auth/login" />} />
                    <Route path="/auth/login" element={<Login />} />
                    <Route path="/auth/signup" element={<SignUp />} />

                    <Route path="/vacations/calendar" element={<Calendar />} />

                    <Route path="/admin/vacation-request" element={<VacationList />} />
                    <Route path="/admin/code" element={<CodeManagement />} />
                    <Route path="/admin/vacation-detail/:id" element={<VacationDetail />} />
                    <Route path="/vacations" element={<Vacations />} />

                    <Route path="/approval/first" element={<FirstApprovalList />} />
                    <Route path="/approval/second" element={<SecondApprovalList />} />
                    <Route path="/approval/first/:approvalStepId" element={<FirstApprovalDetail />} />
                    <Route path="/approval/second/:approvalStepId" element={<SecondApprovalDetail />} />
                    <Route path="/admin/member-approvals" element={<MemberApprovalList />} />

                    <Route path="/vacation-detail/:id" element={<VacationDetail />} />
                    <Route path="/vacations/my" element={<MyVacationStatus />} />
                    <Route path="/vacations/request" element={<VacationRequest />} />
                    <Route path="/vacations/history" element={<VacationHistory />} />
                    <Route path="/vacations/edit/:requestId" element={<VacationEdit />} />
                    <Route path="/admin/vacations/statistics" element={<VacationManagerPage />} />
                </Routes>
            </DefaultLayout>
        </Router>
    );
}

export default App;