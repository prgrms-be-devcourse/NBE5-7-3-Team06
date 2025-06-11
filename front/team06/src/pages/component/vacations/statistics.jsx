import React, { useState, useEffect } from 'react';
import axios from '../../../api/axiosInstance';

const VacationManagerPage = () => {
    const [searchName, setSearchName] = useState('');
    const [vacationData, setVacationData] = useState([]);
    const [loading, setLoading] = useState(true);
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);
    const [selectedYear, setSelectedYear] = useState('2025');
    const [vacationTypesList, setVacationTypesList] = useState([]);
    const [selectedVacationType, setSelectedVacationType] = useState('');
    const [departments, setDepartments] = useState([]);
    const [selectedDepartment, setSelectedDepartment] = useState('');

    const vacationTypes = ['january', 'february', 'march', 'april', 'may', 'june', 'july', 'august', 'september', 'october', 'november', 'december'];

    useEffect(() => {
        fetchVacationTypes();
        fetchDepartments();
    }, []);

    useEffect(() => {
        if (selectedVacationType !== '') {
            fetchVacationData();
        }
    }, [currentPage, selectedYear, selectedVacationType, selectedDepartment]);

    const fetchVacationTypes = async () => {
        try {
            const response = await axios.get('/admin/code', {
                params: { groupCode: 'VACATION_TYPE' },
            });

            const types = response.data?.codeReadResponse?.content;

            const filteredTypes = Array.isArray(types)
                ? types.filter(type => type.code !== '05')
                : [];

            if (filteredTypes.length > 0) {
                setVacationTypesList(filteredTypes);
                setSelectedVacationType(filteredTypes[0].code);
            } else {
                console.error('휴가 타입 응답 형식이 올바르지 않습니다.', response.data);
                setVacationTypesList([]);
                setSelectedVacationType('');
            }
        } catch (error) {
            console.error('휴가 타입 불러오기 실패:', error);
            setVacationTypesList([]);
            setSelectedVacationType('');
        }
    };

    const fetchDepartments = async () => {
        try {
            const res = await axios.get('http://localhost:8080/depts');
            setDepartments(res.data);
        } catch {
            alert('부서 목록을 불러오는 데 실패했습니다.');
            setDepartments([]);
        }
    };

    const fetchVacationData = async () => {
        setLoading(true);
        try {
            const response = await axios.get('/admin/vacations/statistics', {
                params: {
                    year: selectedYear,
                    name: searchName || undefined,
                    vacationCode: selectedVacationType || undefined,
                    deptId: selectedDepartment || undefined,
                    page: currentPage,
                },
            });
            setVacationData(response.data.content);
            setTotalPages(response.data.totalPages);
        } catch (error) {
            console.error('휴가 데이터 불러오기 실패:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleSearch = () => {
        setCurrentPage(0);
        fetchVacationData();
    };

    const handlePageChange = (page) => {
        if (page >= 0 && page < totalPages) {
            setCurrentPage(page);
        }
    };

    const handleYearChange = (e) => {
        setSelectedYear(e.target.value);
    };

    return (
        <div className="p-6 bg-gray-50 min-h-screen">
            <div className="max-w-7xl mx-auto">
                <div className="mb-6">
                    <h1 className="text-2xl font-bold text-gray-900">휴가 현황</h1>
                </div>
            </div>

            {/* 필터 영역 */}
            <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
                <div className="flex flex-wrap items-center gap-6 mb-4">
                    {/* 연도 선택 */}
                    <div className="flex items-center gap-2">
                        <label htmlFor="year" className="text-gray-700 whitespace-nowrap">년도</label>
                        <select
                            id="year"
                            value={selectedYear}
                            onChange={handleYearChange}
                            className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                        >
                            {[2025, 2024, 2023, 2022, 2021, 2020, 2019, 2018].map(year => (
                                <option key={year} value={year}>{year}</option>
                            ))}
                        </select>
                    </div>

                    {/* 휴가 종류 선택 */}
                    <div className="flex items-center gap-2">
                        <label htmlFor="vacationType" className="text-gray-700 whitespace-nowrap">휴가 종류</label>
                        <select
                            id="vacationType"
                            value={selectedVacationType}
                            onChange={(e) => setSelectedVacationType(e.target.value)}
                            className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                        >
                            {vacationTypesList.map((type) => (
                                <option key={type.code} value={type.code}>{type.name}</option>
                            ))}
                        </select>
                    </div>

                    {/* 부서 선택 */}
                    <div className="flex items-center gap-2">
                        <label htmlFor="department" className="text-gray-700 whitespace-nowrap">부서</label>
                        <select
                            id="department"
                            value={selectedDepartment}
                            onChange={(e) => setSelectedDepartment(e.target.value)}
                            className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                        >
                            <option value="">전체</option>
                            {departments.map((dept) => (
                                <option key={dept.id} value={dept.id}>{dept.name}</option>
                            ))}
                        </select>
                    </div>
                </div>

                {/* 이름 검색 필터 */}
                <div className="flex items-center gap-2">
                    <input
                        type="text"
                        value={searchName}
                        placeholder="이름 검색"
                        onChange={(e) => setSearchName(e.target.value)}
                        onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                        className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 w-64"
                    />
                    <button
                        onClick={handleSearch}
                        className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition"
                    >
                        검색
                    </button>
                </div>
            </div>

            {/* 테이블 */}
            <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-x-auto">
                {loading ? (
                    <div className="flex justify-center items-center py-12">
                        <div className="animate-spin text-blue-500 text-xl">⏳</div>
                        <span className="ml-2 text-gray-600">데이터를 불러오는 중...</span>
                    </div>
                ) : (
                    <table className="w-full text-center border-collapse">
                        <thead>
                        <tr className="bg-gray-100">
                            <th className="px-4 py-3 border-b border-gray-300">이름</th>
                            <th className="px-4 py-3 border-b border-gray-300">부여 휴가</th>
                            {vacationTypes.map((_, i) => (
                                <th key={i} className="px-4 py-3 border-b border-gray-300">{i + 1}월</th>
                            ))}
                            <th className="px-4 py-3 border-b border-gray-300">사용</th>
                            <th className="px-4 py-3 border-b border-gray-300">잔여</th>
                        </tr>
                        </thead>
                        <tbody>
                        {vacationData.map((emp, index) => (
                            <tr key={emp.memberId} className={index % 2 === 0 ? 'bg-white' : 'bg-gray-50'}>
                                <td className="px-4 py-3 border-b border-gray-200">{emp.userName}</td>
                                <td className="px-4 py-3 border-b border-gray-200">{emp.totalCount}</td>
                                {vacationTypes.map((type, i) => (
                                    <td key={i} className="px-4 py-3 border-b border-gray-200">{emp[type]}</td>
                                ))}
                                <td className="px-4 py-3 border-b border-gray-200">{emp.usedCount}</td>
                                <td className="px-4 py-3 border-b border-gray-200">{emp.remainCount}</td>
                            </tr>
                        ))}
                        {vacationData.length === 0 && (
                            <tr>
                                <td colSpan={15} className="px-4 py-8 text-center text-gray-500 border-b border-gray-200">
                                    검색 결과가 없습니다.
                                </td>
                            </tr>
                        )}
                        </tbody>
                    </table>
                )}

                {/* 페이지네이션 */}
                {!loading && vacationData.length > 0 && (
                    <div className="flex justify-center items-center gap-4 mt-6">
                        <button
                            onClick={() => handlePageChange(currentPage - 1)}
                            disabled={currentPage === 0}
                            className="px-3 py-2 bg-white border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 disabled:opacity-50"
                        >
                            이전
                        </button>

                        <div className="flex items-center">
                            <span className="px-3 py-1 bg-blue-100 text-blue-700 font-medium rounded-md">
                                {currentPage + 1}
                            </span>
                            <span className="mx-2 text-gray-600">/</span>
                            <span className="text-gray-700">{totalPages}</span>
                        </div>

                        <button
                            onClick={() => handlePageChange(currentPage + 1)}
                            disabled={currentPage >= totalPages - 1}
                            className="px-3 py-2 bg-white border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 disabled:opacity-50"
                        >
                            다음
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
};

export default VacationManagerPage;
