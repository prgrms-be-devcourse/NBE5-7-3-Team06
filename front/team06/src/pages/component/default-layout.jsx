import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import api from '../../api/axiosInstance';

const handleLogout = async () => {
    try {
        const ok = window.confirm('로그아웃하시겠습니까?');
        if (!ok) return;
        await api.post('/auth/logout'); // ✅ refreshToken → 서버로 전달됨 (withCredentials=true)

        localStorage.clear(); // ✅ accessToken, userName 등 제거
        window.location.href = '/auth/login'; // 또는 navigate('/auth/login')
    } catch (e) {
        console.error('로그아웃 실패', e);
    }
};

const DefaultLayout = ({ children }) => {
    const navigate = useNavigate();
    const location = useLocation();

    const isAuthPage = location.pathname === '/auth/login' || location.pathname === '/auth/signup';
    // 로그인, 회원가입에서는 sidebar, 이름 정보 삭제

    const [sidebarCollapsed, setSidebarCollapsed] = useState(true);

    const activeMenu =
        location.pathname === '/vacations/my' ? 'my-vacation' :
        location.pathname === '/vacations/history' ? 'vacation-history' :
        location.pathname === '/vacations/request' ? 'vacation-request' :
        location.pathname.includes('/vacations/calendar') ? 'vacation-calendar' :
        location.pathname.includes('/admin/vacation-request') ? 'vacation-list' :
        location.pathname.includes('/admin/code') ? 'code-management' :
        location.pathname.includes('/approval/first') ? 'approval-first' :
        location.pathname.includes('/approval/second') ? 'approval-second' :
        location.pathname.includes('/admin/member-approvals') ? 'member-approval' :
        location.pathname.includes('/admin/vacations/statistics') ? 'vacation-statistics' :
        location.pathname.includes('/vacations') ? 'vacations' :
        'vacation-list';

    const [userName, setUserName] = useState('');
    const [firstLetter, setFirstLetter] = useState('');
    useEffect(() => {
        const storedName = localStorage.getItem('userName');
        if (storedName) {
            setUserName(storedName);
            setFirstLetter(storedName?.charAt(0));
        }
    }, []);

    const getMenuIdsByRole = (role) => {
        switch (role) {
            case 'ADMIN':
                return ['vacation-calendar', 'vacation-list', 'code-management', 'member-approval', 'vacation-statistics', 'vacations'];
            case 'USER':
                return ['vacation-calendar', 'my-vacation', 'vacation-history', 'vacation-request', 'approval-first', 'approval-second'];
            default:
                return ['vacation-calendar'];
        }
    };

    // 메뉴 아이템 정의
    const menuItems = [
        {
            id: 'vacation-calendar',
            label: '휴가 일정 캘린더',
            icon: '🗓️',
            path: '/vacations/calendar'
        },
        {
            id: 'my-vacation',
            label: '내 연차 정보',
            icon: '🏖️',
            path: '/vacations/my'
        },
        {
            id: 'vacation-history',
            label: '휴가 신청 내역',
            icon: '📋',
            path: '/vacations/history'
        },
        {
            id: 'vacation-request',
            label: '휴가 신청',
            icon: '✏️',
            path: '/vacations/request'
        },
        {
            id: 'approval-first',
            label: '1차 결재 목록',
            icon: '📝',
            path: '/approval/first'
        },
        {
            id: 'approval-second',
            label: '2차 결재 목록',
            icon: '📝',
            path: '/approval/second'
        },
        {
            id: 'vacation-list',
            label: '휴가 신청 목록',
            icon: '📋',
            path: '/admin/vacation-request'
        },
        {
            id: 'vacation-statistics',
            label: '휴가 현황',
            icon: '📝',
            path: '/admin/vacations/statistics'
        },
        {
            id: 'vacations',
            label: '휴가 관리',
            icon: '✅',
            path: '/vacations'
        },
        {
            id: 'code-management',
            label: '코드 관리',
            icon: '⚙️',
            path: '/admin/code'
        },
        {
            id: 'member-approval',
            label: '회원 승인 관리',
            icon: '👤',
            path: '/admin/member-approvals'
        },
    ];

    // 사용자 권한에 따른 메뉴 필터링
    const userRole = localStorage.getItem('userRole'); // 예: 'ADMIN'
    const allowedMenuIds = getMenuIdsByRole(userRole);
    const filteredMenuItems = menuItems.filter(item => allowedMenuIds.includes(item.id));

    const handleMenuClick = (path) => {
        navigate(path);
    };

    return (
        <div className="min-h-screen bg-gray-50">
            {/* Header */}
            <header className="bg-white shadow-sm border-b border-gray-200 h-16 fixed top-0 left-0 right-0 z-30">
                <div className="flex items-center justify-between h-full px-4">
                    {/* Left side - Logo and toggle */}
                    <div className="flex items-center">
                        <button
                            onClick={() => setSidebarCollapsed(!sidebarCollapsed)}
                            className="p-2 rounded-md hover:bg-gray-100 lg:hidden"
                        >
                            {!isAuthPage && (
                                <span className="text-gray-600">☰</span>
                            )}
                        </button>
                        <div className="ml-4 flex items-center">
                            <h1 className="text-xl font-bold text-gray-900">휴가 관리 시스템</h1>
                        </div>
                    </div>

                    {/* Right side - User menu */}
                    {!isAuthPage && (
                        <div className="flex items-center space-x-4">
                            <div className="flex items-center space-x-3">
                                <div className="w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center">
                                    <span className="text-white text-sm font-medium">{firstLetter}</span>
                                </div>
                                <div className="hidden md:block">
                                    <div className="font-medium text-gray-900">{userName}</div>
                                </div>
                                <button
                                    onClick={handleLogout}
                                    className="mt-1 font-medium text-gray-500 hover:text-gray-800 underline"
                                    style={{ margin: '5px' }}
                                >
                                    로그아웃
                                </button>
                            </div>
                        </div>
                    )}
                </div>
            </header>

            {/* Sidebar */}
            {!isAuthPage && (
                <aside className={`fixed top-16 left-0 h-full bg-white shadow-lg border-r border-gray-200 transition-width duration-300 z-20 ${
                    sidebarCollapsed ? 'w-16' : 'w-60'
                }`}>
                    {/* Toggle button for desktop */}
                    <div className="hidden lg:block absolute -right-3 top-6">
                        <button
                            onClick={() => setSidebarCollapsed(!sidebarCollapsed)}
                            className="w-6 h-6 bg-white rounded-full border border-gray-300 flex items-center justify-center hover:bg-gray-50"
                        >
                            <span className={`text-xs transition-transform ${sidebarCollapsed ? 'rotate-180' : ''}`}>
                              ◀
                            </span>
                        </button>
                    </div>

                    {/* Navigation - 필터링된 메뉴 항목 사용 */}
                    <nav className="p-4">
                        <ul className="space-y-2">
                            {filteredMenuItems.map((item) => (
                                <li key={item.id}>
                                    <button
                                        onClick={() => handleMenuClick(item.path)}
                                        className={`w-full flex items-center px-3 py-2 rounded-md text-sm transition-colors ${
                                            activeMenu === item.id
                                                ? 'bg-blue-50 text-blue-700 border-l-4 border-blue-700'
                                                : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                                        }`}
                                        title={sidebarCollapsed ? item.label : ''}
                                    >
                                        <span className="text-lg">{item.icon}</span>
                                        {!sidebarCollapsed && (
                                            <span className="ml-3 font-medium">{item.label}</span>
                                        )}
                                    </button>
                                </li>
                            ))}
                        </ul>
                    </nav>

                    {/* Sidebar footer */}
                    <div className="absolute bottom-4 left-4 right-4">
                        <div className={`text-xs text-gray-500 ${sidebarCollapsed ? 'hidden' : 'block'}`}>
                            <div>버전: 1.0.0</div>
                            <div className="mt-1">© 2025 Company</div>
                        </div>
                    </div>
                </aside>
            )}

            {/* Main content */}
            <main className={`pt-16 transition-all duration-300 ${
                isAuthPage ? '' :sidebarCollapsed ? 'ml-16' : 'ml-60'
            }`}>
                <div className="min-h-screen">
                    {children}
                </div>
            </main>

            {/* Footer */}
            <footer className={`bg-white border-t border-gray-200 transition-all duration-300 ${
                isAuthPage ? '':sidebarCollapsed ? 'ml-16' : 'ml-60'
            }`}>
                <div className="px-6 py-4">
                    <div className="flex flex-col md:flex-row md:items-center md:justify-between">
                        <div className="text-sm text-gray-600">
                            © 2025 휴가 관리 시스템. All rights reserved.
                        </div>
                        <div className="mt-2 md:mt-0 flex space-x-6 text-sm text-gray-600">
                            <a href="#" className="hover:text-gray-900">개인정보처리방침</a>
                            <a href="#" className="hover:text-gray-900">이용약관</a>
                            <a href="#" className="hover:text-gray-900">도움말</a>
                        </div>
                    </div>
                </div>
            </footer>

            {/* Mobile sidebar overlay */}
            {!isAuthPage && !sidebarCollapsed && (
                <div
                    className="lg:hidden fixed inset-0 bg-black bg-opacity-50 z-10"
                    onClick={() => setSidebarCollapsed(true)}
                ></div>
            )}
        </div>
    );
};

export default DefaultLayout;