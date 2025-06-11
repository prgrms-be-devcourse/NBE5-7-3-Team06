import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import api from "../../../api/axiosInstance";

const SecondApprovalList = () => {
    // 상태 관리
    const [approvals, setApprovals] = useState([]);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    // 페이징 상태
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [itemsPerPage, setItemsPerPage] = useState(0);
    // const itemsPerPage = 20;

    // 필터 상태
    const [filters, setFilters] = useState({
        type: "",
        name: "",
        from: "",
        to: "",
        status: "PENDING"
    });

    // 실제 API 요청에 사용될 필터 상태
    const [appliedFilters, setAppliedFilters] = useState({
        type: "",
        name: "",
        from: "",
        to: "",
        status: "PENDING"
    });

    // 휴가 종류 옵션 - 반차 옵션 분리
    const vacationTypes = [
        { value: "연차", label: "연차" },
        { value: "포상 휴가", label: "포상 휴가" },
        { value: "공가", label: "공가" },
        { value: "경조사 휴가", label: "경조사 휴가" },
        { value: "반차(오전)", label: "반차(오전)" },
        { value: "반차(오후)", label: "반차(오후)" }
    ];

    // 부서 옵션
    const departments = [
        { value: "인사팀", label: "인사팀" },
        { value: "개발팀", label: "개발팀" },
        { value: "기획팀", label: "기획팀" }
    ];

    // 직급 옵션
    const positions = [
        { value: "사원", label: "사원" },
        { value: "주임", label: "주임" },
        { value: "과장", label: "과장" },
        { value: "대리", label: "대리" }
    ];

    // 결재 상태 옵션
    const statusOptions = [
        { value: "PENDING", label: "결재 대기" },
        { value: "WAITING", label: "1차 결재 대기" },
        { value: "APPROVED", label: "승인" },
        { value: "REJECTED", label: "반려" },
        { value: "CANCELED", label: "취소" }
    ];

    // 백엔드에서 결재 데이터 가져오기
    const fetchApprovals = async () => {
        setLoading(true);
        try {
            let url = `/approval-steps/second?page=${currentPage}`;
            const queryParams = new URLSearchParams();

            if (appliedFilters.type) queryParams.append("type", appliedFilters.type);
            if (appliedFilters.name) queryParams.append("name", appliedFilters.name);
            if (appliedFilters.from) queryParams.append("from", `${appliedFilters.from}T00:00:00`);
            if (appliedFilters.to) queryParams.append("to", `${appliedFilters.to}T23:59:59`);
            if (appliedFilters.status) queryParams.append("status", appliedFilters.status);

            const queryString = queryParams.toString();
            if (queryString) {
                url += `&${queryString}`;
            }

            // ✅ 공통 axios 인스턴스로 GET 요청
            const response = await api.get(url);
            const data = response.data;

            // 휴가 종류를 백엔드 데이터에 맞게 처리
            const processedApprovals = data.content.map(approval => {
                // 반차의 경우 오전/오후 구분 추가
                if (approval.type.includes("반차")) {
                    const isAM = new Date(approval.from).getHours() < 12;
                    return {
                        ...approval,
                        displayType: isAM ? "반차(오전)" : "반차(오후)",
                        // 반차는 0.5일로 계산
                        vacationDays: 0.5
                    };
                } else {
                    // 일반 휴가의 경우 일수 계산
                    const days = calculateDays(approval.from, approval.to);
                    return {
                        ...approval,
                        displayType: approval.type,
                        vacationDays: days
                    };
                }
            });

            setApprovals(processedApprovals);
            setTotalPages(data.totalPages);
            setTotalElements(data.totalElements);
            setCurrentPage(data.number);
            setItemsPerPage(data.size);
        } catch (error) {
            console.error("Error fetching approvals:", error);
            alert(error.response?.data?.message || "데이터를 불러오는데 실패했습니다.");
        } finally {
            setLoading(false);
        }
    };


    // currentPage나 appliedFilters가 변경될 때만 데이터 로드 (필터 입력 변경 시 API 호출 없음)
    useEffect(() => {
        fetchApprovals();
    }, [appliedFilters, currentPage]);

    // 검색 버튼 클릭 핸들러 - 이때만 실제 필터 적용
    const handleSearch = () => {
        setCurrentPage(0); // 첫 페이지로 이동
        setAppliedFilters({...filters}); // 사용자가 입력한 필터 값을 적용
    };

    // 필터 변경 핸들러 - 입력만 받고 API 호출 안 함
    const handleFilterChange = (key, value) => {
        setFilters(prev => ({ ...prev, [key]: value }));
    };

    // 페이지 변경 핸들러
    const handlePageChange = (page) => {
        setCurrentPage(page);
    };

    // 상세 화면으로 이동하는 함수
    const handleRowClick = (approvalStepId) => {
        console.log(`상세 정보 보기: ${approvalStepId}`);
        navigate(`/approval/second/${approvalStepId}`);
    };

    const handleReset = () => {
        const emptyFilters = {
            type: "",
            name: "",
            from: "",
            to: "",
            status: "PENDING"
        };
        setFilters(emptyFilters);
        setAppliedFilters(emptyFilters);
        setCurrentPage(0);
    };


    // 날짜 형식 변환 함수
    const formatDate = (dateString) => {
        if (!dateString) return "";
        const date = new Date(dateString);
        return date.toLocaleDateString("ko-KR", {
            year: "numeric",
            month: "2-digit",
            day: "2-digit"
        });
    };

    // 두 날짜 사이의 일수 계산 함수
    const calculateDays = (fromDate, toDate) => {
        if (!fromDate || !toDate) return 0;

        const start = new Date(fromDate);
        const end = new Date(toDate);

        // 시간 부분을 제외하고 날짜만 비교하기 위해 시간을 00:00:00으로 설정
        start.setHours(0, 0, 0, 0);
        end.setHours(0, 0, 0, 0);

        // ms -> days 변환 후 +1 (시작일과 종료일 모두 포함)
        const diffTime = Math.abs(end - start);
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;

        return diffDays;
    };

    // 상태별 뱃지 스타일
    const getStatusBadge = (status) => {
        const statusStyles = {
            PENDING: "bg-yellow-100 text-yellow-800",
            WAITING: "bg-blue-100 text-blue-800",
            APPROVED: "bg-green-100 text-green-800",
            REJECTED: "bg-red-100 text-red-800",
            CANCELED: "bg-gray-100 text-gray-800"
        };
        return statusStyles[status] || "bg-gray-100 text-gray-800";
    };

    // 상태 표시명 변환
    const getStatusDisplayName = (status) => {
        const statusNames = {
            PENDING: "결재 대기",
            WAITING: "1차 결재 대기",
            APPROVED: "승인",
            REJECTED: "반려",
            CANCELED: "취소"
        };
        return statusNames[status] || status;
    };

    // 휴가 타입별 뱃지 스타일
    const getVacationTypeBadge = (type) => {
        if (type.includes("연차")) {
            return "bg-blue-100 text-blue-800";
        } else if (type.includes("포상")) {
            return "bg-purple-100 text-purple-800";
        } else if (type.includes("공가")) {
            return "bg-green-100 text-green-800";
        } else if (type.includes("경조사")) {
            return "bg-orange-100 text-orange-800";
        } else if (type.includes("반차")) {
            return "bg-teal-100 text-teal-800";
        } else {
            return "bg-gray-100 text-gray-800";
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
                    <h1 className="text-2xl font-bold text-gray-900">2차 결재 요청 목록</h1>
                    <p className="text-gray-600 mt-2">
                        2차 결재가 필요한 휴가 신청 목록을 조회하고 관리할 수 있습니다.
                    </p>
                </div>

                {/* 필터 영역 */}
                <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
                    <h2 className="text-lg font-semibold text-gray-800 mb-4">
                        검색 필터
                    </h2>
                    <div className="space-y-4">
                        {/* 첫 번째 줄 */}
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    휴가 종류
                                </label>
                                <select
                                    value={filters.type}
                                    onChange={(e) => handleFilterChange("type", e.target.value)}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                >
                                    <option value="">전체</option>
                                    {vacationTypes.map((type) => (
                                        <option key={type.value} value={type.value}>
                                            {type.label}
                                        </option>
                                    ))}
                                </select>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    신청자 이름
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
                                    2차 결재 상태
                                </label>
                                <select
                                    value={filters.status}
                                    onChange={(e) => handleFilterChange("status", e.target.value)}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                >
                                    <option value="">전체</option>
                                    {statusOptions.map((status) => (
                                        <option key={status.value} value={status.value}>
                                            {status.label}
                                        </option>
                                    ))}
                                </select>
                            </div>
                        </div>

                        {/* 두 번째 줄 */}
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    시작일
                                </label>
                                <input
                                    type="date"
                                    value={filters.from}
                                    onChange={(e) => handleFilterChange("from", e.target.value)}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    종료일
                                </label>
                                <input
                                    type="date"
                                    value={filters.to}
                                    onChange={(e) => handleFilterChange("to", e.target.value)}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                />
                            </div>
                        </div>

                        {/* 검색 버튼 */}
                        <div className="flex justify-center pt-4 gap-2">
                            <button
                                onClick={handleSearch}
                                className="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
                            >
                                검색
                            </button>
                            <button
                                onClick={handleReset}
                                className="px-6 py-2 bg-gray-200 text-gray-800 rounded-md hover:bg-gray-300 focus:outline-none focus:ring-2 focus:ring-gray-400"
                            >
                                초기화
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
                                        휴가 종류
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        기간
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        신청 일수
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        신청자
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        부서
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        직급
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        1차 결재 상태
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        2차 결재 상태
                                    </th>
                                </tr>
                                </thead>
                                <tbody className="bg-white divide-y divide-gray-200">
                                {approvals.length > 0 ? (
                                    approvals.map((approval) => (
                                        <tr
                                            key={approval.approvalStepId}
                                            className="hover:bg-gray-50 cursor-pointer"
                                            onClick={() => handleRowClick(approval.approvalStepId)}
                                        >
                                            <td className="px-6 py-4 whitespace-nowrap">
                                                <span
                                                    className={`inline-flex px-2 py-1 text-xs font-medium rounded-full ${getVacationTypeBadge(
                                                        approval.type
                                                    )}`}
                                                >
                                                    {approval.displayType || approval.type}
                                                </span>
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                                                {formatDate(approval.from)} ~ {formatDate(approval.to)}
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                                                {approval.vacationDays}일
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap">
                                                <div className="text-sm font-medium text-gray-900">
                                                    {approval.name}
                                                </div>
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap">
                                                <div className="text-sm text-gray-900">
                                                    {approval.deptName}
                                                </div>
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap">
                                                <div className="text-sm text-gray-900">
                                                    {approval.positionName}
                                                </div>
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap">
                                                <span
                                                    className={`inline-flex px-2 py-1 text-xs font-medium rounded-full ${getStatusBadge(
                                                        approval.firstApprovalStatus
                                                    )}`}
                                                >
                                                    {getStatusDisplayName(approval.firstApprovalStatus)}
                                                </span>
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap">
                                                <span
                                                    className={`inline-flex px-2 py-1 text-xs font-medium rounded-full ${getStatusBadge(
                                                        approval.secondApprovalStatus || approval.status
                                                    )}`}
                                                >
                                                    {getStatusDisplayName(approval.secondApprovalStatus || approval.status)}
                                                </span>
                                            </td>
                                        </tr>
                                    ))
                                ) : (
                                    <tr>
                                        <td colSpan="8" className="px-6 py-4 text-center text-gray-500">
                                            결재 요청 목록이 없습니다.
                                        </td>
                                    </tr>
                                )}
                                </tbody>
                            </table>
                        )}
                    </div>

                    {/* 페이지네이션 */}
                    {totalElements > 0 && (
                        <div className="bg-white px-6 py-3 border-t border-gray-200">
                            <div className="flex items-center justify-between">
                                <div className="text-sm text-gray-700">
                                    {currentPage * itemsPerPage + 1} - {Math.min((currentPage + 1) * itemsPerPage, totalElements)} /
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
                    )}
                </div>
            </div>
        </div>
    );
};

export default SecondApprovalList;