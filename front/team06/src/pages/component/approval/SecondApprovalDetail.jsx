import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import api from "../../../api/axiosInstance";

const SecondApprovalDetail = () => {
    const { approvalStepId } = useParams();
    const navigate = useNavigate();

    // 상태 관리
    const [approvalDetail, setApprovalDetail] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [rejectReason, setRejectReason] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [showRejectModal, setShowRejectModal] = useState(false);
    // 승인 모달 상태 추가
    const [showApproveModal, setShowApproveModal] = useState(false);

    // 휴가 상세 정보 가져오기
    const fetchApprovalDetail = async () => {
        setLoading(true);
        try {
            const response = await api.get(`/approval-steps/second/${approvalStepId}`);

            // 데이터 처리 - 반차 구분과 휴가 일수 계산
            const data = response.data;
            let processedData = { ...data };

            // 반차의 경우 오전/오후 구분 추가
            if (data.type.includes("반차")) {
                const isAM = new Date(data.from).getHours() < 12;
                processedData.displayType = isAM ? "반차(오전)" : "반차(오후)";
                processedData.vacationDays = 0.5;
            } else {
                // 일반 휴가의 경우 일수 계산
                const days = calculateDays(data.from, data.to);
                processedData.displayType = data.type;
                processedData.vacationDays = days;
            }

            setApprovalDetail(processedData);
        } catch (err) {
            console.error("Error fetching approval detail:", err);
            setError(err.response?.data?.message || "데이터를 불러오는데 실패했습니다.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        window.scrollTo(0, 0);
        fetchApprovalDetail();
    }, [approvalStepId]);

    // 승인 모달 표시
    const openApproveModal = () => {
        setShowApproveModal(true);
    };

    // 승인 처리
    const handleApprove = async () => {
        setIsSubmitting(true);
        try {
            const response = await api.patch(`/approval-steps/second/${approvalStepId}/approve`);
            const result = response.data;

            if (result === true) {
                alert("휴가 신청이 승인되었습니다.");
            } else {
                alert("잔여 연차를 초과하여 승인할 수 없습니다.");
            }
            setShowApproveModal(false);
            fetchApprovalDetail(); // 데이터 새로고침
        } catch (err) {
            console.error("Error approving vacation:", err);
            alert(err.response?.data?.message || "승인 처리 중 오류가 발생했습니다.");
        } finally {
            setIsSubmitting(false);
        }
    };

    // 반려 모달 표시
    const openRejectModal = () => {
        setShowRejectModal(true);
    };

    // 반려 처리
    const handleReject = async () => {
        if (!rejectReason.trim()) {
            alert("반려 사유를 입력해 주세요.");
            return;
        }

        setIsSubmitting(true);
        try {
            await api.patch(`/approval-steps/second/${approvalStepId}/reject`, {
                reason: rejectReason,
            });

            alert("휴가 신청이 반려되었습니다.");
            setShowRejectModal(false);
            setRejectReason(""); // 입력 초기화
            fetchApprovalDetail(); // 데이터 새로고침
        } catch (err) {
            console.error("Error rejecting vacation:", err);
            alert(err.response?.data?.message || "반려 처리 중 오류가 발생했습니다.");
        } finally {
            setIsSubmitting(false);
        }
    };

    // 목록으로 돌아가기
    const handleGoBack = () => {
        navigate(-1);
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

    // 두 날짜 사이의 일수 계산 함수 추가
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

    // 휴가 종류별 뱃지 스타일
    const getVacationTypeBadge = (type) => {
        if (!type) return "bg-gray-100 text-gray-800";

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

    // 상태별 뱃지 스타일
    const getStatusBadge = (status) => {
        const statusStyles = {
            PENDING: "bg-yellow-100 text-yellow-800",
            APPROVED: "bg-green-100 text-green-800",
            REJECTED: "bg-red-100 text-red-800",
            CANCELED: "bg-gray-100 text-gray-800",
            WAITING: "bg-blue-100 text-blue-800"
        };
        return statusStyles[status] || "bg-gray-100 text-gray-800";
    };

    // 상태 표시명 변환
    const getStatusDisplayName = (status) => {
        const statusNames = {
            PENDING: "결재 대기",
            APPROVED: "승인",
            REJECTED: "반려",
            CANCELED: "취소",
            WAITING: "1차 결재 대기중"
        };
        return statusNames[status] || status;
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center h-screen bg-gray-50">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
                <span className="ml-3 text-lg text-gray-600">데이터를 불러오는 중...</span>
            </div>
        );
    }

    if (error) {
        return (
            <div className="p-6 bg-gray-50 min-h-screen">
                <div className="max-w-3xl mx-auto bg-white rounded-lg shadow-sm border border-gray-200 p-6">
                    <div className="text-center py-10">
                        <div className="text-red-600 text-xl mb-4">오류가 발생했습니다</div>
                        <p className="text-gray-600 mb-6">{error}</p>
                        <button
                            onClick={handleGoBack}
                            className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
                        >
                            목록으로 돌아가기
                        </button>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="p-6 bg-gray-50 min-h-screen">
            <div className="max-w-3xl mx-auto">
                {/* 페이지 헤더 */}
                <div className="mb-6 flex justify-between items-center">
                    <h1 className="text-2xl font-bold text-gray-900">2차 결재 상세 정보</h1>
                    <button
                        onClick={handleGoBack}
                        className="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-100 focus:outline-none"
                    >
                        목록으로
                    </button>
                </div>

                {/* 상세 정보 카드 */}
                {approvalDetail && (
                    <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
                        {/* 헤더 섹션 - 상태 표시 */}
                        <div className="bg-gray-50 px-6 py-4 border-b border-gray-200">
                            <div className="flex justify-between items-center">
                                <div>
                                    <span className={`inline-flex px-3 py-1 text-sm font-medium rounded-full ${getVacationTypeBadge(approvalDetail.type)}`}>
                                        {approvalDetail.displayType || approvalDetail.type}
                                    </span>
                                    <span className="ml-3 text-gray-700">
                                        {formatDate(approvalDetail.from)} ~ {formatDate(approvalDetail.to)}
                                    </span>
                                </div>
                                <span className={`inline-flex px-3 py-1 text-sm font-medium rounded-full ${getStatusBadge(approvalDetail.status)}`}>
                                    {getStatusDisplayName(approvalDetail.status)}
                                </span>
                            </div>
                        </div>

                        {/* 내용 섹션 */}
                        <div className="p-6">
                            {/* 신청자 정보 */}
                            <div className="mb-6">
                                <h3 className="text-lg font-semibold text-gray-800 mb-4">신청자 정보</h3>
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                    <div>
                                        <p className="text-sm text-gray-500">신청자</p>
                                        <p className="text-gray-900">{approvalDetail.name}</p>
                                    </div>
                                    <div>
                                        <p className="text-sm text-gray-500">부서</p>
                                        <p className="text-gray-900">{approvalDetail.deptName}</p>
                                    </div>
                                    <div>
                                        <p className="text-sm text-gray-500">직급</p>
                                        <p className="text-gray-900">{approvalDetail.positionName}</p>
                                    </div>
                                    <div>
                                        <p className="text-sm text-gray-500">결재자</p>
                                        <p className="text-gray-900">{approvalDetail.approverName}</p>
                                    </div>
                                </div>
                            </div>

                            {/* 구분선 */}
                            <hr className="my-6 border-gray-200" />

                            {/* 휴가 정보 */}
                            <div className="mb-6">
                                <h3 className="text-lg font-semibold text-gray-800 mb-4">휴가 정보</h3>
                                <div className="grid grid-cols-1 gap-4">
                                    <div>
                                        <p className="text-sm text-gray-500">휴가 기간</p>
                                        <p className="text-gray-900">
                                            {approvalDetail.type.includes("반차") ? (
                                                formatDate(approvalDetail.from)
                                            ) : (
                                                <>
                                                    {formatDate(approvalDetail.from)} ~ {formatDate(approvalDetail.to)}
                                                </>
                                            )}
                                        </p>
                                    </div>
                                    <div>
                                        <p className="text-sm text-gray-500">신청 일수</p>
                                        <p className="text-gray-900">
                                            {approvalDetail.vacationDays}일
                                        </p>
                                    </div>
                                    <div>
                                        <p className="text-sm text-gray-500">휴가 사유</p>
                                        <p className="text-gray-900 whitespace-pre-line">
                                            {approvalDetail.reason || "-"}
                                        </p>
                                    </div>
                                </div>
                            </div>

                            {/* 결재 정보 - 상태가 REJECTED일 때만 보임 */}
                            {approvalDetail.status === "REJECTED" && (
                                <>
                                    <hr className="my-6 border-gray-200" />
                                    <div className="mb-6">
                                        <h3 className="text-lg font-semibold text-gray-800 mb-4">반려 정보</h3>
                                        <div>
                                            <p className="text-sm text-gray-500">반려 사유</p>
                                            <p className="text-gray-900 whitespace-pre-line">
                                                {approvalDetail.approvalReason || "-"}
                                            </p>
                                        </div>
                                    </div>
                                </>
                            )}

                            {/* 결재 버튼 영역 - 상태가 PENDING일 때만 보임 */}
                            {approvalDetail.status === "PENDING" && (
                                <div className="mt-8 flex justify-center space-x-4">
                                    <button
                                        onClick={openApproveModal}
                                        disabled={isSubmitting}
                                        className="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
                                    >
                                        {isSubmitting ? "처리 중..." : "승인하기"}
                                    </button>
                                    <button
                                        onClick={openRejectModal}
                                        disabled={isSubmitting}
                                        className="px-6 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500 disabled:opacity-50 disabled:cursor-not-allowed"
                                    >
                                        {isSubmitting ? "처리 중..." : "반려하기"}
                                    </button>
                                </div>
                            )}

                            {/* WAITING 상태일 때 메시지 표시 */}
                            {approvalDetail.status === "WAITING" && (
                                <div className="mt-6 p-4 bg-blue-50 rounded-md border border-blue-200">
                                    <p className="text-blue-800 text-center">
                                        1차 결재가 완료되지 않았습니다. 1차 결재 완료 후 처리할 수 있습니다.
                                    </p>
                                </div>
                            )}
                        </div>
                    </div>
                )}

                {/* 승인 확인 모달 */}
                {showApproveModal && (
                    <div className="fixed inset-0 bg-black/20 backdrop-blur-sm z-50 flex justify-center items-center transition-all duration-200">
                        <div className="bg-white rounded-lg shadow-2xl border border-gray-200 max-w-md w-full mx-4">
                            <div className="px-6 py-4 border-b border-gray-200">
                                <h3 className="text-lg font-semibold text-gray-800">휴가 승인 확인</h3>
                            </div>
                            <div className="p-6">
                                <p className="text-gray-700 mb-4">
                                    이 휴가 신청을 승인하시겠습니까?
                                </p>
                                <div className="flex justify-end space-x-3">
                                    <button
                                        onClick={() => setShowApproveModal(false)}
                                        className="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-100 focus:outline-none"
                                    >
                                        취소
                                    </button>
                                    <button
                                        onClick={handleApprove}
                                        disabled={isSubmitting}
                                        className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
                                    >
                                        {isSubmitting ? "처리 중..." : "승인"}
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                )}

                {/* 반려 사유 모달 */}
                {showRejectModal && (
                    <div className="fixed inset-0 bg-black/20 backdrop-blur-sm z-50 flex justify-center items-center transition-all duration-200">
                        <div className="bg-white rounded-lg shadow-2xl border border-gray-200 max-w-lg w-full mx-4">
                            <div className="px-6 py-4 border-b border-gray-200">
                                <h3 className="text-lg font-semibold text-gray-800">반려 사유 입력</h3>
                            </div>
                            <div className="p-6">
                                <div className="mb-4">
                                    <label className="block text-sm font-medium text-gray-700 mb-2">
                                        반려 사유 <span className="text-red-600">*</span>
                                    </label>
                                    <textarea
                                        value={rejectReason}
                                        onChange={(e) => setRejectReason(e.target.value)}
                                        rows="4"
                                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                        placeholder="반려 사유를 입력해주세요."
                                    ></textarea>
                                </div>
                                <div className="flex justify-end space-x-3">
                                    <button
                                        onClick={() => setShowRejectModal(false)}
                                        className="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-100 focus:outline-none"
                                    >
                                        취소
                                    </button>
                                    <button
                                        onClick={handleReject}
                                        disabled={isSubmitting || !rejectReason.trim()}
                                        className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500 disabled:opacity-50 disabled:cursor-not-allowed"
                                    >
                                        {isSubmitting ? "처리 중..." : "반려하기"}
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default SecondApprovalDetail;