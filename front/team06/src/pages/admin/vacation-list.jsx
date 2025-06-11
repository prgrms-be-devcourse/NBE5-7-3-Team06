import React, {useEffect, useState} from "react";
import {useNavigate} from "react-router-dom";
import api from "../../api/axiosInstance";

export default function VacationList() {
    const navigate = useNavigate();

    // 상태 관리
    const [vacations, setVacations] = useState([]);
    const [loading, setLoading] = useState(false);
    const [dropdownContents, setDropdownContents] = useState({
        positionCodes: [],
        vacationTypeCodes: [],
        quarters: [],
    });

    // 페이징 상태
    const [currentPage, setCurrentPage] = useState(0); // 백엔드는 0부터 시작
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const itemsPerPage = 20;

    // 필터 상태
    const [filters, setFilters] = useState({
        name: "",
        deptName: "",
        vacationTypeCodeId: "",
        positionCodeId: "",
        year: "",
        quarter: "",
        start: "",
        end: "",
    });

    // 백엔드에서 휴가 데이터 가져오기
    const fetchVacations = async () => {
        setLoading(true);
        try {
            // 쿼리 파라미터 구성
            const queryParams = new URLSearchParams({
                page: currentPage.toString(),
                size: itemsPerPage.toString(),
                sort: "createdAt,desc",
            });

            // 필터 조건 추가
            if (filters.name) queryParams.append("name", filters.name);
            if (filters.deptName)
                queryParams.append("deptName", filters.deptName);
            if (filters.vacationTypeCodeId) queryParams.append("vacationTypeCodeId", filters.vacationTypeCodeId);
            if (filters.positionCodeId) queryParams.append("positionCodeId", filters.positionCodeId);
            if (filters.year) queryParams.append("year", filters.year);
            if (filters.quarter)
                queryParams.append("quarter", filters.quarter);
            if (filters.start) queryParams.append("start", filters.start);
            if (filters.end) queryParams.append("end", filters.end);

            // 변경 후 (api 인스턴스 사용)
            const response = await api.get(`/admin/vacation-request?${queryParams}`);
            console.log(response);
            const data = response.data;
            console.log(data);

            setVacations(data.vacationRequestSearchResponses.content);
            setTotalPages(data.vacationRequestSearchResponses.pageable.totalPages);
            setTotalElements(data.vacationRequestSearchResponses.totalElements);
            setDropdownContents(data.dropdownContents);
        } catch (error) {
            console.error("Error fetching vacations:", error);
            alert("데이터를 불러오는데 실패했습니다.");
        } finally {
            setLoading(false);
        }
    };

    // 초기 데이터 로드
    useEffect(() => {
        fetchVacations();
    }, [currentPage]);

    // 검색 버튼 클릭 핸들러
    const handleSearch = () => {
        setCurrentPage(0); // 첫 페이지로 이동
        fetchVacations();
    };

    // 필터 변경 핸들러
    const handleFilterChange = (key, value) => {
        setFilters((prev) => ({...prev, [key]: value}));
    };

    // 페이지 변경 핸들러
    const handlePageChange = (page) => {
        setCurrentPage(page);
    };

    // 상세 화면으로 이동하는 함수
    const handleDetailView = (vacationId) => {
        navigate(`/admin/vacation-detail/${vacationId}`);
    };

    // 상태별 뱃지 스타일
    const getStatusBadge = (status) => {
        const statusStyles = {
            IN_PROGRESS: "bg-yellow-100 text-yellow-800",
            APPROVED: "bg-green-100 text-green-800",
            REJECTED: "bg-red-100 text-red-800",
            CANCELED: "bg-gray-100 text-gray-800",
        };
        return statusStyles[status] || "bg-gray-100 text-gray-800";
    };

    // 상태 표시명 변환
    const getStatusDisplayName = (status) => {
        const statusNames = {
            IN_PROGRESS: "대기",
            APPROVED: "승인",
            REJECTED: "반려",
            CANCELED: "취소",
        };
        return statusNames[status] || status;
    };

    // 일수 계산 (반차 포함)
    const calculateDays = (from, to, type) => {
        if (!from || !to) return 0;

        // 반차인 경우 0.5일 반환
        if (type.includes('반차')) {
            return 0.5;
        }

        // 날짜만 추출하여 비교 (시간 정보 제거)
        const fromDate = new Date(from.split('T')[0]);
        const toDate = new Date(to.split('T')[0]);

        // 날짜 차이 계산 (밀리초 단위)
        const diffTime = Math.abs(toDate - fromDate);

        // 일 단위로 변환
        const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));

        // 시작일도 포함하므로 +1
        return diffDays + 1;
    };

    // 날짜 포맷팅 함수 (날짜만)
    const formatDateOnly = (dateString) => {
        if (!dateString) return "";
        const date = new Date(dateString);
        return date.toLocaleDateString("ko-KR", {
            year: "numeric",
            month: "2-digit",
            day: "2-digit",
        });
    };
    // 휴가 종류별 뱃지 스타일
    const getVacationTypeBadge = (type) => {
        if (type.includes("연차")) {
            return "bg-blue-100 text-blue-800";
        } else if (type.includes("포상")) {
            return "bg-purple-100 text-purple-800";
        } else if (type.includes("공가")) {
            return "bg-green-100 text-green-800";
        } else if (type.includes("경조사")) {
            return "bg-orange-100 text-orange-800";
        } else {
            return "bg-gray-100 text-gray-800"; // 기타
        }
    };

    // 페이지네이션 렌더링
    const renderPagination = () => {
        const pages = [];
        const maxVisiblePages = 10;
        let startPage = Math.max(0, currentPage - Math.floor(maxVisiblePages / 2));
        let endPage = Math.min(totalPages - 1, startPage + maxVisiblePages - 1);

        if (endPage - startPage + 1 < maxVisiblePages && startPage > 0) {
            startPage = Math.max(0, endPage - maxVisiblePages + 1);
        }

        for (let i = startPage; i <= endPage; i++) {
            pages.push(
                <button
                    key={i}
                    onClick={() => handlePageChange(i)}
                    className={`px-3 py-1 mx-1 rounded ${
                        i === currentPage
                            ? "bg-blue-500 text-white"
                            : "bg-white text-blue-500 border border-blue-500 hover:bg-blue-50"
                    }`}
                >
                    {i + 1}
                </button>
            );
        }

        return pages;
    };

    return (
        <div className="p-6 bg-gray-50 min-h-screen">
            <div className="max-w-7xl mx-auto">
                {/* 페이지 헤더 */}
                <div className="mb-6">
                    <h1 className="text-2xl font-bold text-gray-900">휴가 신청 목록</h1>
                    <p className="text-gray-600 mt-2">
                        전체 휴가 신청 내역을 조회하고 관리할 수 있습니다.
                    </p>
                </div>

                {/* 필터 영역 */}
                <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
                    <h2 className="text-lg font-semibold text-gray-800 mb-4">
                        검색 필터
                    </h2>
                    <div className="space-y-4">
                        {/* 첫 번째 줄 */}
                        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    사용자 이름
                                </label>
                                <input
                                    type="text"
                                    value={filters.name}
                                    onChange={(e) => handleFilterChange("name", e.target.value)}
                                    placeholder="이름 입력"
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    부서
                                </label>
                                <input
                                    type="text"
                                    value={filters.deptName}
                                    onChange={(e) =>
                                        handleFilterChange("deptName", e.target.value)
                                    }
                                    placeholder="부서명 입력"
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    휴가 종류
                                </label>
                                <select
                                    value={filters.vacationTypeCodeId}
                                    onChange={(e) =>
                                        handleFilterChange("vacationTypeCodeId", e.target.value)
                                    }
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                >
                                    <option value="">전체</option>
                                    {dropdownContents.vacationTypeCodes.map((code) => (
                                        <option key={code.id} value={code.id}>
                                            {code.name}
                                        </option>
                                    ))}
                                </select>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    직책
                                </label>
                                <select
                                    value={filters.positionCodeId}
                                    onChange={(e) =>
                                        handleFilterChange("positionCodeId", e.target.value)
                                    }
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                >
                                    <option value="">전체</option>
                                    {dropdownContents.positionCodes.map((code) => (
                                        <option key={code.id} value={code.id}>
                                            {code.name}
                                        </option>
                                    ))}
                                </select>
                            </div>
                        </div>

                        {/* 두 번째 줄 */}
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    년도
                                </label>
                                <select
                                    value={filters.year}
                                    onChange={(e) => handleFilterChange("year", e.target.value)}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                >
                                    <option value="" selected>
                                        전체
                                    </option>
                                    <option value="2020">2020</option>
                                    <option value="2021">2021</option>
                                    <option value="2022">2022</option>
                                    <option value="2023">2023</option>
                                    <option value="2024">2024</option>
                                    <option value="2025">2025</option>
                                </select>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    분기
                                </label>
                                <select
                                    value={filters.quarter}
                                    onChange={(e) =>
                                        handleFilterChange("quarter", e.target.value)
                                    }
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                >
                                    <option value="NONE" selected>
                                        전체
                                    </option>
                                    <option value="Q1">1분기</option>
                                    <option value="Q2">2분기</option>
                                    <option value="Q3">3분기</option>
                                    <option value="Q4">4분기</option>
                                    <option value="H1">상반기</option>
                                    <option value="H2">하반기</option>
                                </select>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    시작일
                                </label>
                                <input
                                    type="date"
                                    value={filters.start}
                                    onChange={(e) => handleFilterChange("start", e.target.value)}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    종료일
                                </label>
                                <input
                                    type="date"
                                    value={filters.end}
                                    onChange={(e) => handleFilterChange("end", e.target.value)}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                />
                            </div>
                        </div>

                        {/* 검색 버튼 */}
                        <div className="flex justify-center pt-4">
                            <button
                                onClick={handleSearch}
                                className="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
                            >
                                검색
                            </button>
                        </div>
                    </div>
                </div>

                {/* 테이블 영역 */}
                <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
                    <div className="p-6 border-b border-gray-200">
                        <div className="flex justify-between items-center">
                            <h3 className="text-lg font-semibold text-gray-800">
                                전체 {totalElements}건
                            </h3>
                        </div>
                    </div>

                    <div className="overflow-x-auto">
                        {loading ? (
                            <div className="flex justify-center items-center h-64">
                                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
                                <span className="ml-3 text-gray-600">
                  데이터를 불러오는 중...
                </span>
                            </div>
                        ) : (
                            <table className="w-full">
                                <thead className="bg-gray-50">
                                <tr>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        신청자
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        부서
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        휴가 종류
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        휴가 기간
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        일수
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        승인자
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        상태
                                    </th>
                                </tr>
                                </thead>
                                <tbody className="bg-white divide-y divide-gray-200">
                                {vacations.map((vacation) => (
                                    <tr
                                        key={vacation.id}
                                        className="hover:bg-gray-50 cursor-pointer"
                                        onClick={() => handleDetailView(vacation.id)}
                                    >
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <div className="text-sm font-medium text-gray-900">
                                                {vacation.applicantName}
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <div className="text-sm text-gray-900">
                                                {vacation.deptName}
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                        <span
                            className={`inline-flex px-2 py-1 text-xs font-medium rounded-full ${getVacationTypeBadge(
                                vacation.type
                            )}`}
                        >
                          {vacation.type}
                        </span>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                                            {formatDateOnly(vacation.from)} ~{" "}
                                            {formatDateOnly(vacation.to)}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                                            {calculateDays(
                                                vacation.from,
                                                vacation.to,
                                                vacation.type
                                            )}
                                            일
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                                            {vacation.approverNames.join(", ")}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                        <span
                            className={`inline-flex px-2 py-1 text-xs font-medium rounded-full ${getStatusBadge(
                                vacation.status
                            )}`}
                        >
                          {getStatusDisplayName(vacation.status)}
                        </span>
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        )}
                    </div>

                    {/* 페이지네이션 */}
                    <div className="bg-white px-6 py-3 border-t border-gray-200">
                        <div className="flex items-center justify-between">
                            <div className="text-sm text-gray-700">
                                {currentPage * itemsPerPage + 1} -{" "}
                                {Math.min((currentPage + 1) * itemsPerPage, totalElements)} /
                                전체 {totalElements}건
                            </div>
                            <div className="flex items-center">
                                <button
                                    onClick={() => handlePageChange(Math.max(0, currentPage - 1))}
                                    disabled={currentPage === 0}
                                    className="px-3 py-1 mx-1 text-blue-600 border border-blue-600 rounded hover:bg-blue-50 disabled:opacity-50 disabled:cursor-not-allowed"
                                >
                                    이전
                                </button>
                                {renderPagination()}
                                <button
                                    onClick={() =>
                                        handlePageChange(Math.min(totalPages - 1, currentPage + 1))
                                    }
                                    disabled={currentPage === totalPages - 1}
                                    className="px-3 py-1 mx-1 text-blue-600 border border-blue-600 rounded hover:bg-blue-50 disabled:opacity-50 disabled:cursor-not-allowed"
                                >
                                    다음
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
