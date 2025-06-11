import React, { useState, useEffect, useRef } from "react";
import api from "../../../api/axiosInstance";

export default function MemberApprovalList() {
    // 상태 관리
    const [memberRequests, setMemberRequests] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selectedMemberId, setSelectedMemberId] = useState(null);
    const [showConfirmModal, setShowConfirmModal] = useState(false);
    const [modalAction, setModalAction] = useState(''); // 'approve' 또는 'reject'
    const [isSubmitting, setIsSubmitting] = useState(false);

    // 회원 승인 요청 목록 조회
    const fetchMemberRequests = async () => {
        setLoading(true);
        try {
            const response = await api.get("/admin/member-approvals");
            setMemberRequests(response.data);
        } catch (err) {
            console.error("Error fetching member requests:", err);
            setError(err.response?.data?.message || "데이터를 불러오는데 실패했습니다.");
        } finally {
            setLoading(false);
        }
    };

    // 컴포넌트 마운트시 데이터 로드 및 스크롤 상단으로 이동
    useEffect(() => {
        fetchMemberRequests().then(() => {
            setTimeout(() => {
                window.scrollTo({ top: 0, behavior: 'auto' });
            }, 0); // 즉시 실행 (렌더 이후)
        });
    }, []);

    // 승인 모달 열기
    const openApproveModal = (memberId) => {
        setSelectedMemberId(memberId);
        setModalAction('approve');
        setShowConfirmModal(true);
    };

    // 거부 모달 열기
    const openRejectModal = (memberId) => {
        setSelectedMemberId(memberId);
        setModalAction('reject');
        setShowConfirmModal(true);
    };

    // 모달 닫기
    const closeModal = () => {
        setShowConfirmModal(false);
        setSelectedMemberId(null);
        setModalAction('');
    };

    // 회원 승인 처리
    const handleApprove = async () => {
        if (!selectedMemberId) return;

        setIsSubmitting(true);
        try {
            await api.post(`/admin/member-approvals/${selectedMemberId}`);
            alert("회원 가입이 승인되었습니다.");
            closeModal();
            fetchMemberRequests();
            window.scrollTo({ top: 0, behavior: "auto" });
        } catch (err) {
            console.error("Error approving member:", err);
            alert(err.response?.data?.message || "회원 승인 처리 중 오류가 발생했습니다.");
        } finally {
            setIsSubmitting(false);
        }
    };

    // 회원 거부(삭제) 처리
    const handleReject = async () => {
        if (!selectedMemberId) return;

        setIsSubmitting(true);
        try {
            await api.delete(`/admin/member-approvals/${selectedMemberId}`);
            alert("회원 가입이 거부되었습니다.");
            closeModal();
            fetchMemberRequests();
            window.scrollTo({ top: 0, behavior: "auto" });
        } catch (err) {
            console.error("Error rejecting member:", err);
            alert(err.response?.data?.message || "회원 거부 처리 중 오류가 발생했습니다.");
        } finally {
            setIsSubmitting(false);
        }
    };

    // 모달 액션 처리
    const handleConfirmAction = () => {
        if (modalAction === 'approve') {
            handleApprove();
        } else if (modalAction === 'reject') {
            handleReject();
        }
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
                <div className="max-w-4xl mx-auto bg-white rounded-lg shadow-sm border border-gray-200 p-6">
                    <div className="text-center py-10">
                        <div className="text-red-600 text-xl mb-4">오류가 발생했습니다</div>
                        <p className="text-gray-600 mb-6">{error}</p>
                        <button
                            onClick={() => {
                                window.scrollTo({
                                    top: 0,
                                    behavior: 'auto'
                                });
                                fetchMemberRequests();
                            }}
                            className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
                        >
                            다시 시도
                        </button>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="p-6 bg-gray-50 min-h-screen">
            <div id="page-top" className="max-w-5xl mx-auto">
                {/* 페이지 헤더 */}
                <div className="mb-6">
                    <h1 className="text-2xl font-bold text-gray-900">회원가입 승인 관리</h1>
                    <p className="text-gray-600 mt-1">회원가입 요청 목록을 확인하고 승인 또는 거부할 수 있습니다.</p>
                </div>

                {/* 회원 목록 테이블 */}
                <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
                    {memberRequests.length === 0 ? (
                        <div className="p-6 text-center text-gray-500">
                            현재 처리할 회원가입 요청이 없습니다.
                        </div>
                    ) : (
                        <div className="overflow-x-auto">
                            <table className="min-w-full divide-y divide-gray-200">
                                <thead className="bg-gray-50">
                                <tr>
                                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">성명</th>
                                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">직급</th>
                                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">부서</th>
                                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">생년월일</th>
                                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">이메일</th>
                                    <th scope="col" className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">관리</th>
                                </tr>
                                </thead>
                                <tbody className="bg-white divide-y divide-gray-200">
                                {memberRequests.map((member) => (
                                    <tr key={member.memberId} className="hover:bg-gray-50">
                                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{member.name}</td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{member.positionName}</td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{member.deptName}</td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                            {member.birth &&
                                                `${member.birth.substring(0, 2)}/${member.birth.substring(2, 4)}/${member.birth.substring(4, 6)}`
                                            }
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{member.email}</td>
                                        <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                                            <button
                                                onClick={() => openApproveModal(member.memberId)}
                                                className="text-blue-600 hover:text-blue-900 mr-4 bg-blue-100 px-3 py-1 rounded-md"
                                            >
                                                승인
                                            </button>
                                            <button
                                                onClick={() => openRejectModal(member.memberId)}
                                                className="text-red-600 hover:text-red-900 bg-red-100 px-3 py-1 rounded-md"
                                            >
                                                삭제
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>

                {/* 확인 모달 */}
                {showConfirmModal && (
                    <div className="fixed inset-0 bg-black/20 backdrop-blur-sm z-50 flex justify-center items-center transition-all duration-200">
                        <div className="bg-white rounded-lg shadow-2xl border border-gray-200 max-w-md w-full mx-4">
                            <div className="px-6 py-4 border-b border-gray-200">
                                <h3 className="text-lg font-semibold text-gray-800">
                                    {modalAction === 'approve' ? '회원 승인 확인' : '회원 삭제 확인'}
                                </h3>
                            </div>
                            <div className="p-6">
                                <p className="text-gray-700 mb-4">
                                    {modalAction === 'approve'
                                        ? '해당 회원의 가입 요청을 승인하시겠습니까?'
                                        : '해당 회원의 가입 요청을 거부하고 삭제하시겠습니까?'}
                                </p>
                                <div className="flex justify-end space-x-3">
                                    <button
                                        onClick={closeModal}
                                        className="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-100 focus:outline-none"
                                    >
                                        취소
                                    </button>
                                    <button
                                        onClick={handleConfirmAction}
                                        disabled={isSubmitting}
                                        className={`px-4 py-2 text-white rounded-md focus:outline-none focus:ring-2 disabled:opacity-50 disabled:cursor-not-allowed ${
                                            modalAction === 'approve'
                                                ? 'bg-blue-600 hover:bg-blue-700 focus:ring-blue-500'
                                                : 'bg-red-600 hover:bg-red-700 focus:ring-red-500'
                                        }`}
                                    >
                                        {isSubmitting ? "처리 중..." : modalAction === 'approve' ? "승인" : "삭제"}
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}