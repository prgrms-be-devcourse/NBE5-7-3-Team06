import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../../api/axiosInstance';

const VacationHistory = () => {
    const navigate = useNavigate();

    // 상태 관리
    const [vacations, setVacations] = useState([]);
    const [loading, setLoading] = useState(true);
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const ITEMS_PER_PAGE = 20;

    // 휴가 내역 가져오기
    useEffect(() => {
        const fetchVacations = async () => {
            try {
                setLoading(true);
                const response = await api.get('/vacations', {
                    params: { page: currentPage }
                });

                // 데이터 받아온 후 최신순으로 정렬
                setVacations(response.data.content || []);
                setTotalPages(response.data.totalPages || 0);
                setTotalElements(response.data.totalElements || 0);
            } catch (err) {
                console.error('휴가 내역 조회 실패:', err);
                alert('휴가 내역을 불러오는데 실패했습니다.');
            } finally {
                setLoading(false);
            }
        };

        fetchVacations();
    }, [currentPage]);

    // 페이지 변경 처리
    const handlePageChange = (page) => {
        setCurrentPage(page);
    };

    // 새 휴가 신청 페이지로 이동
    const handleNewRequest = () => {
        navigate('/vacations/request');
    };

    // 내 연차 정보 페이지로 이동
    const handleBackToInfo = () => {
        navigate('/vacations/my');
    };

    // 휴가 상세 정보 페이지로 이동
    const handleViewDetail = (requestId) => {
        navigate(`/vacation-detail/${requestId}`);
    };

    // 휴가 신청 수정 페이지로 이동
    const handleEditRequest = (requestId) => {
        navigate(`/vacations/edit/${requestId}`);
    };

    // 휴가 신청 취소
    const handleCancelRequest = async (requestId) => {
        if (!window.confirm('휴가 신청을 취소하시겠습니까?')) {
            return;
        }

        try {
            await api.delete(`/vacations/${requestId}`);
            alert('휴가 신청이 취소되었습니다.');

            // 현재 페이지 다시 로드
            const response = await api.get('/vacations', {
                params: { page: currentPage }
            });

            // 데이터를 다시 최신순으로 정렬
            const sortedVacations = [...(response.data.content || [])].sort((a, b) => {
                return b.requestId - a.requestId;
            });

            setVacations(sortedVacations);
            setTotalPages(response.data.totalPages || 0);
            setTotalElements(response.data.totalElements || 0);
        } catch (err) {
            console.error('휴가 취소 실패:', err);
            alert('휴가 취소 중 오류가 발생했습니다.');
        }
    };

    // 상태별 뱃지 스타일
    const getStatusBadge = (status) => {
        const statusStyles = {
            'IN_PROGRESS': 'bg-yellow-100 text-yellow-800',
            'APPROVED': 'bg-green-100 text-green-800',
            'REJECTED': 'bg-red-100 text-red-800',
            'CANCELED': 'bg-gray-100 text-gray-800'
        };

        const statusNames = {
            'IN_PROGRESS': '대기',
            'APPROVED': '승인',
            'REJECTED': '반려',
            'CANCELED': '취소'
        };

        return (
            <span className={`inline-flex px-2 py-1 text-xs font-medium rounded-full ${statusStyles[status] || 'bg-gray-100 text-gray-800'}`}>
                {statusNames[status] || status}
            </span>
        );
    };

    // 날짜 포맷팅
    const formatDate = (dateString) => {
        if (!dateString) return '';

        try {
            // 시간대 오차 문제 방지를 위해 년-월-일만 추출
            const date = new Date(dateString);
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');

            return `${year}. ${month}. ${day}.`;
        } catch (e) {
            console.error('날짜 형식 오류:', e);
            return dateString; // 에러 발생 시 원본 문자열 반환
        }
    };

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

    // 휴가 수정/취소 가능 여부 확인
    const canEditOrCancel = (status) => {
        return status === 'IN_PROGRESS';
    };

    // 페이지 번호 생성 함수 - 페이지네이션 UI를 위한 배열 생성
    const generatePageNumbers = () => {
        const pageNumbers = [];
        const maxVisiblePages = 5; // 한 번에 보여줄 최대 페이지 수

        // 총 페이지 수가 최대 표시 페이지 수보다 작거나 같은 경우
        if (totalPages <= maxVisiblePages) {
            for (let i = 0; i < totalPages; i++) {
                pageNumbers.push(i);
            }
            return pageNumbers;
        }

        // 시작 페이지와 끝 페이지 계산
        let startPage = Math.max(0, currentPage - Math.floor(maxVisiblePages / 2));
        let endPage = startPage + maxVisiblePages - 1;

        // 끝 페이지가 총 페이지수를 초과하는 경우 조정
        if (endPage >= totalPages) {
            endPage = totalPages - 1;
            startPage = Math.max(0, endPage - maxVisiblePages + 1);
        }

        for (let i = startPage; i <= endPage; i++) {
            pageNumbers.push(i);
        }

        return pageNumbers;
    };

    return (
        <div className="min-h-screen bg-gray-50 p-6">
            <div className="max-w-6xl mx-auto">
                {/* 헤더 */}
                <div className="flex flex-col md:flex-row md:justify-between md:items-center mb-6 gap-4">
                    <div>
                        <h1 className="text-2xl font-bold text-gray-800">휴가 신청 내역</h1>
                        <p className="text-gray-600 mt-1">신청한 모든 휴가 내역을 확인할 수 있습니다. (최신순 정렬)</p>
                    </div>
                    <div className="flex gap-3">
                        <button
                            onClick={handleBackToInfo}
                            className="px-4 py-2 bg-white border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 hover:bg-gray-50"
                        >
                            내 연차 정보
                        </button>
                        <button
                            onClick={handleNewRequest}
                            className="px-4 py-2 bg-blue-600 border border-transparent rounded-md shadow-sm text-sm font-medium text-white hover:bg-blue-700"
                        >
                            + 휴가 신청
                        </button>
                    </div>
                </div>

                {/* 내역 테이블 카드 */}
                <div className="bg-white rounded-lg shadow-md overflow-hidden mb-6">
                    {/* 로딩 상태 */}
                    {loading ? (
                        <div className="flex flex-col items-center justify-center h-64">
                            <div className="w-12 h-12 rounded-full border-2 border-t-blue-500 border-b-blue-500 border-l-gray-200 border-r-gray-200 animate-spin"></div>
                            <p className="mt-4 text-gray-600 font-medium">휴가 내역을 불러오는 중입니다...</p>
                        </div>
                    ) : (
                        <>
                            {/* 내역이 없는 경우 */}
                            {vacations.length === 0 ? (
                                <div className="flex flex-col items-center justify-center h-64">
                                    <div className="text-5xl mb-4">📋</div>
                                    <p className="text-lg text-gray-600 mb-4">휴가 신청 내역이 없습니다.</p>
                                    <button
                                        onClick={handleNewRequest}
                                        className="px-4 py-2 bg-blue-600 border border-transparent rounded-md shadow-sm text-sm font-medium text-white hover:bg-blue-700"
                                    >
                                        첫 휴가 신청하기
                                    </button>
                                </div>
                            ) : (
                                <>
                                    {/* 내역 테이블 */}
                                    <div className="overflow-x-auto">
                                        <table className="min-w-full divide-y divide-gray-200">
                                            <thead className="bg-gray-50">
                                            <tr>
                                                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-16">
                                                    번호
                                                </th>
                                                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                    휴가 유형
                                                </th>
                                                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                    기간
                                                </th>
                                                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                    신청 일수
                                                </th>
                                                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                    사유
                                                </th>
                                                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                    결재자
                                                </th>
                                                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                    상태
                                                </th>
                                                <th scope="col" className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                                                    관리
                                                </th>
                                            </tr>
                                            </thead>
                                            <tbody className="bg-white divide-y divide-gray-200">
                                            {vacations.map((vacation, index) => (
                                                <tr
                                                    key={vacation.requestId}
                                                    className="hover:bg-gray-50 cursor-pointer"
                                                    onClick={() => handleViewDetail(vacation.requestId)}
                                                >
                                                    <td className="px-6 py-4 whitespace-nowrap">
                                                        <div className="text-sm text-gray-900">
                                                            {/* 순서대로 번호 매기기 */}
                                                            {currentPage * ITEMS_PER_PAGE + index + 1}
                                                        </div>
                                                    </td>
                                                    <td className="px-6 py-4 whitespace-nowrap">
                                                        <div
                                                            className="text-sm font-medium text-gray-900">{vacation.vacationType}</div>
                                                    </td>
                                                    <td className="px-6 py-4 whitespace-nowrap">
                                                        <div className="text-sm text-gray-900">
                                                            {formatDate(vacation.from)} ~ {formatDate(vacation.to)}
                                                        </div>
                                                    </td>
                                                    <td className="px-6 py-4 whitespace-nowrap">
                                                        <div className="text-sm text-gray-900">
                                                            {calculateDays(vacation.from, vacation.to, vacation.vacationType)}일
                                                        </div>
                                                    </td>
                                                    <td className="px-6 py-4 whitespace-nowrap max-w-xs">
                                                        <div className="text-sm text-gray-900 truncate"
                                                             title={vacation.reason}>
                                                            {vacation.reason}
                                                        </div>
                                                    </td>
                                                    <td className="px-6 py-4 whitespace-nowrap">
                                                        <div
                                                            className="text-sm text-gray-900">{vacation.approverName}</div>
                                                    </td>
                                                    <td className="px-6 py-4 whitespace-nowrap">
                                                        {getStatusBadge(vacation.approvalStatus)}
                                                    </td>
                                                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                                                        <div className="flex justify-end space-x-2">
                                                            {/* 상세 보기 버튼 추가 */}
                                                            <button
                                                                onClick={(e) => {
                                                                    e.stopPropagation(); // 행 클릭 이벤트 전파 방지
                                                                    handleViewDetail(vacation.requestId);
                                                                }}
                                                                className="text-blue-600 hover:text-blue-900 bg-blue-50 px-2 py-1 rounded"
                                                            >
                                                                상세
                                                            </button>

                                                            {/* 기존 수정/취소 버튼 (대기 상태일 때만 표시) */}
                                                            {canEditOrCancel(vacation.approvalStatus) && (
                                                                <>
                                                                    <button
                                                                        onClick={(e) => {
                                                                            e.stopPropagation(); // 행 클릭 이벤트 전파 방지
                                                                            handleEditRequest(vacation.requestId);
                                                                        }}
                                                                        className="text-blue-600 hover:text-blue-900 bg-blue-50 px-2 py-1 rounded"
                                                                    >
                                                                        수정
                                                                    </button>
                                                                    <button
                                                                        onClick={(e) => {
                                                                            e.stopPropagation(); // 행 클릭 이벤트 전파 방지
                                                                            handleCancelRequest(vacation.requestId);
                                                                        }}
                                                                        className="text-red-600 hover:text-red-900 bg-red-50 px-2 py-1 rounded"
                                                                    >
                                                                        취소
                                                                    </button>
                                                                </>
                                                            )}
                                                        </div>
                                                    </td>
                                                </tr>
                                            ))}
                                            </tbody>
                                        </table>
                                    </div>

                                    {/* 개선된 페이지네이션 */}
                                    {totalPages > 1 && (
                                        <div className="bg-gray-50 px-6 py-4 border-t border-gray-200 flex justify-between items-center">
                                            <div className="text-sm text-gray-700">
                                                전체 {totalElements}건 중 {currentPage * ITEMS_PER_PAGE + 1}-{Math.min((currentPage + 1) * ITEMS_PER_PAGE, totalElements)}
                                            </div>
                                            <div className="flex items-center space-x-1">
                                                {/* 첫 페이지 버튼 */}
                                                <button
                                                    onClick={() => handlePageChange(0)}
                                                    disabled={currentPage === 0}
                                                    className="px-2 py-1 rounded-md text-sm font-medium text-gray-700 bg-white border border-gray-300 disabled:opacity-50"
                                                    title="첫 페이지"
                                                >
                                                    &laquo;
                                                </button>

                                                {/* 이전 페이지 버튼 */}
                                                <button
                                                    onClick={() => handlePageChange(Math.max(0, currentPage - 1))}
                                                    disabled={currentPage === 0}
                                                    className="px-2 py-1 rounded-md text-sm font-medium text-gray-700 bg-white border border-gray-300 disabled:opacity-50"
                                                >
                                                    &lt;
                                                </button>

                                                {/* 페이지 번호 버튼들 */}
                                                {generatePageNumbers().map(pageNum => (
                                                    <button
                                                        key={pageNum}
                                                        onClick={() => handlePageChange(pageNum)}
                                                        className={`px-3 py-1 rounded-md text-sm font-medium ${
                                                            pageNum === currentPage
                                                                ? 'bg-blue-600 text-white'
                                                                : 'text-gray-700 bg-white border border-gray-300 hover:bg-gray-50'
                                                        }`}
                                                    >
                                                        {pageNum + 1}
                                                    </button>
                                                ))}

                                                {/* 다음 페이지 버튼 */}
                                                <button
                                                    onClick={() => handlePageChange(Math.min(totalPages - 1, currentPage + 1))}
                                                    disabled={currentPage >= totalPages - 1}
                                                    className="px-2 py-1 rounded-md text-sm font-medium text-gray-700 bg-white border border-gray-300 disabled:opacity-50"
                                                >
                                                    &gt;
                                                </button>

                                                {/* 마지막 페이지 버튼 */}
                                                <button
                                                    onClick={() => handlePageChange(totalPages - 1)}
                                                    disabled={currentPage >= totalPages - 1}
                                                    className="px-2 py-1 rounded-md text-sm font-medium text-gray-700 bg-white border border-gray-300 disabled:opacity-50"
                                                    title="마지막 페이지"
                                                >
                                                    &raquo;
                                                </button>
                                            </div>
                                        </div>
                                    )}
                                </>
                            )}
                        </>
                    )}
                </div>

                {/* 안내 카드 */}
                <div className="bg-blue-50 border border-blue-100 rounded-lg p-4">
                    <h3 className="text-sm font-medium text-blue-800 mb-2">안내사항</h3>
                    <ul className="text-sm text-blue-700 space-y-1 pl-5 list-disc">
                        <li>휴가 행을 클릭하거나 '상세' 버튼을 클릭하여 상세 정보를 확인할 수 있습니다.</li>
                        <li>'대기' 상태인 휴가만 수정 및 취소가 가능합니다.</li>
                        <li>이미 승인된 휴가를 취소하려면 관리자에게 문의하세요.</li>
                        <li>휴가 승인은 보통 1-2일 내에 처리됩니다.</li>
                        <li>반려된 휴가는 사유를 확인 후 다시 신청해주세요.</li>
                    </ul>
                </div>
            </div>
        </div>
    );
};

export default VacationHistory;