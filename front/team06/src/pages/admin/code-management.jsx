import React, {useEffect, useState} from 'react';
import api from "../../api/axiosInstance";

export default function CodeManagement() {
    const [codes, setCodes] = useState([]);
    const [groupCodes, setGroupCodes] = useState([]);
    const [loading, setLoading] = useState(false);

    // 페이징 상태
    const [currentPage, setCurrentPage] = useState(0); // 백엔드는 0부터 시작
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const itemsPerPage = 20;

    // 검색 및 필터 상태
    const [selectedGroupCode, setSelectedGroupCode] = useState('');

    // 모달 상태
    const [showModal, setShowModal] = useState(false);
    const [modalMode, setModalMode] = useState('create'); // 'create', 'edit'
    const [selectedCode, setSelectedCode] = useState(null);

    // 폼 상태
    const [formData, setFormData] = useState({
        groupCode: '',
        code: '',
        name: ''
    });

    // 폼 검증 에러
    const [formErrors, setFormErrors] = useState({});

    // 백엔드에서 코드 데이터 가져오기
    const fetchCodes = async () => {
        setLoading(true);
        try {
            // 쿼리 파라미터 구성
            const queryParams = new URLSearchParams({
                page: currentPage.toString(),
                size: itemsPerPage.toString()
            });

            // 그룹코드 필터 추가
            if (selectedGroupCode) {
                queryParams.append('groupCode', selectedGroupCode);
            }

            // console.log('API 호출 URL:', `http://localhost:8080/admin/code?${queryParams}`);

            // const response = await fetch(`http://localhost:8080/admin/code?${queryParams}`);
            const response = await api.get(`/admin/code?${queryParams}`);
            const data = response.data;
            // console.log('Response status:', response.status);

            // if (!response.ok) {
            //   throw new Error(`HTTP error! status: ${response.status}`);
            // }

            // const data = await response.json();
            // console.log('API 응답 데이터:', data);

            // 응답 구조 확인 및 안전하게 접근
            if (data && data.codeReadResponse) {
                setCodes(data.codeReadResponse.content || []);
                setTotalPages(data.codeReadResponse.totalPages || 0);
                setTotalElements(data.codeReadResponse.totalElements || 0);
            } else {
                console.error('예상과 다른 응답 구조:', data);
                setCodes([]);
                setTotalPages(0);
                setTotalElements(0);
            }

            // 그룹코드 목록 설정
            if (data && data.groupCodes) {
                setGroupCodes(data.groupCodes);
            }

        } catch (error) {
            console.error('Error fetching codes:', error);
            alert(`코드 데이터를 불러오는데 실패했습니다: ${error.message}`);
        } finally {
            setLoading(false);
        }
    };

    // 초기 데이터 로드
    useEffect(() => {
        fetchCodes();
    }, [currentPage, selectedGroupCode]);

    // 현재 페이지 데이터는 이미 백엔드에서 페이징된 데이터
    const currentData = codes;

    // 폼 입력 핸들러
    const handleInputChange = (field, value) => {
        setFormData(prev => ({...prev, [field]: value}));
        // 에러 클리어
        if (formErrors[field]) {
            setFormErrors(prev => ({...prev, [field]: ''}));
        }
    };

    // 폼 검증
    const validateForm = () => {
        const errors = {};

        if (!formData.groupCode.trim()) {
            errors.groupCode = '그룹코드를 입력해주세요.';
        }
        if (!formData.code.trim()) {
            errors.code = '코드를 입력해주세요.';
        }
        if (!formData.name.trim()) {
            errors.name = '코드명을 입력해주세요.';
        }

        setFormErrors(errors);
        return Object.keys(errors).length === 0;
    };

    // 모달 열기
    const openModal = (mode, code = null) => {
        setModalMode(mode);
        setSelectedCode(code);

        if (mode === 'create') {
            setFormData({
                groupCode: '',
                code: '',
                name: ''
            });
        } else if (mode === 'edit' && code) {
            setFormData({
                groupCode: code.groupCode,
                code: code.code,
                name: code.name
            });
        }

        setFormErrors({});
        setShowModal(true);
    };

    // 모달 닫기
    const closeModal = () => {
        setShowModal(false);
        setSelectedCode(null);
        setFormErrors({});
    };

    // 저장
    const handleSave = async () => {
        if (!validateForm()) return;

        setLoading(true);

        try {
            let response;

            try {
                if (modalMode === 'create') {
                    // POST 요청으로 새 코드 생성
                    const response = await api.post(`/admin/code`, JSON.stringify(formData), {
                        headers: {
                            'Content-Type': 'application/json'
                        }
                    });
                } else if (modalMode === 'edit') {
                    // PUT 요청으로 코드 수정
                    const response = await api.put(`/admin/code/${selectedCode.id}`, JSON.stringify(formData), {
                        headers: {
                            'Content-Type': 'application/json'
                        }
                    });
                }
            } catch (error) {
                console.error('코드 중복 발생', error);
                alert('이미 존재하는 그룹코드와 코드 조합입니다.');
                closeModal();
            }
            console.log(response);
            // if (!response.ok) {
            //   throw new Error(`코드 ${modalMode === 'create' ? '생성' : '수정'}에 실패했습니다.`);
            // }

            // 성공 시 목록 새로고침
            fetchCodes();
            closeModal();

        } catch (error) {
            console.error('Error saving code:', error);
            alert(error.message);
        } finally {
            setLoading(false);
        }
    };

    // 삭제
    const handleDelete = async (code) => {
        if (window.confirm(`'${code.name}' 코드를 삭제하시겠습니까?`)) {
            setLoading(true);

            try {

                const response = await api.delete(`/admin/code/${code.id}`)

                // if (!response.ok) {
                //   throw new Error('코드 삭제에 실패했습니다.');
                // }

                // 성공 시 목록 새로고침
                fetchCodes();

            } catch (error) {
                console.error('Error deleting code:', error);
                alert(error.message);
            } finally {
                setLoading(false);
            }
        }
    };

    // 페이지 변경
    const handlePageChange = (page) => {
        setCurrentPage(page);
    };

    // 그룹코드 선택
    const handleGroupCodeSelect = (groupCode) => {
        setSelectedGroupCode(groupCode);
        setCurrentPage(0); // 첫 페이지로 이동
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
                            ? 'bg-blue-500 text-white'
                            : 'bg-white text-blue-500 border border-blue-500 hover:bg-blue-50'
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
                    <h1 className="text-2xl font-bold text-gray-900">코드 관리</h1>
                    <p className="text-gray-600 mt-2">시스템에서 사용하는 코드를 관리할 수 있습니다.</p>
                </div>

                {/* 그룹코드 필터 영역 */}
                <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
                    <div className="mb-4">
                        <label className="block text-sm font-medium text-gray-700 mb-2">그룹코드 필터</label>
                        <div className="flex flex-wrap gap-2">
                            <button
                                onClick={() => handleGroupCodeSelect('')}
                                className={`px-3 py-2 rounded-md text-sm font-medium ${
                                    selectedGroupCode === ''
                                        ? 'bg-blue-500 text-white'
                                        : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                                }`}
                            >
                                전체
                            </button>
                            {groupCodes.map((groupCode) => (
                                <button
                                    key={groupCode}
                                    onClick={() => handleGroupCodeSelect(groupCode)}
                                    className={`px-3 py-2 rounded-md text-sm font-medium ${
                                        selectedGroupCode === groupCode
                                            ? 'bg-blue-500 text-white'
                                            : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                                    }`}
                                >
                                    {groupCode}
                                </button>
                            ))}
                        </div>
                    </div>
                </div>

                {/* 테이블 영역 */}
                <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
                    <div className="p-6 border-b border-gray-200">
                        <div className="flex justify-between items-center">
                            <h3 className="text-lg font-semibold text-gray-800">전체 {totalElements}건</h3>
                            <button
                                onClick={() => openModal('create')}
                                className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
                            >
                                + 새 코드 추가
                            </button>
                        </div>
                    </div>

                    <div className="overflow-x-auto">
                        {loading ? (
                            <div className="flex justify-center items-center h-64">
                                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
                                <span className="ml-3 text-gray-600">데이터를 불러오는 중...</span>
                            </div>
                        ) : (
                            <table className="w-full">
                                <thead className="bg-gray-50">
                                <tr>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">그룹코드</th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">코드</th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">코드명</th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">작업</th>
                                </tr>
                                </thead>
                                <tbody className="bg-white divide-y divide-gray-200">
                                {currentData.map((code) => (
                                    <tr key={code.id} className="hover:bg-gray-50">
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                                            {code.id}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                                            {code.groupCode}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                                            {code.code}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                                            {code.name}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                                            <div className="flex space-x-2">
                                                <button
                                                    onClick={() => openModal('edit', code)}
                                                    className="text-blue-600 hover:text-blue-900"
                                                >
                                                    수정
                                                </button>
                                                <button
                                                    onClick={() => handleDelete(code)}
                                                    className="text-red-600 hover:text-red-900"
                                                >
                                                    삭제
                                                </button>
                                            </div>
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
                                    onClick={() => handlePageChange(Math.min(totalPages - 1, currentPage + 1))}
                                    disabled={currentPage === totalPages - 1}
                                    className="px-3 py-1 mx-1 text-blue-600 border border-blue-600 rounded hover:bg-blue-50 disabled:opacity-50 disabled:cursor-not-allowed"
                                >
                                    다음
                                </button>
                            </div>
                        </div>
                    </div>
                </div>

                {/* 모달 */}
                {showModal && (
                    <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
                        <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
                            <div className="mt-3">
                                <h3 className="text-lg font-medium text-gray-900 mb-4">
                                    {modalMode === 'create' ? '새 코드 추가' : '코드 수정'}
                                </h3>

                                <div className="space-y-4">
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            그룹코드 <span className="text-red-500">*</span>
                                        </label>
                                        <input
                                            type="text"
                                            value={formData.groupCode}
                                            onChange={(e) => handleInputChange('groupCode', e.target.value.toUpperCase())}
                                            placeholder="예: VACATION_TYPE"
                                            className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                                                formErrors.groupCode ? 'border-red-500' : 'border-gray-300'
                                            }`}
                                        />
                                        {formErrors.groupCode && (
                                            <p className="text-red-500 text-xs mt-1">{formErrors.groupCode}</p>
                                        )}
                                    </div>

                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            코드 <span className="text-red-500">*</span>
                                        </label>
                                        <input
                                            type="text"
                                            value={formData.code}
                                            onChange={(e) => handleInputChange('code', e.target.value.toUpperCase())}
                                            placeholder="예: ANNUAL"
                                            className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                                                formErrors.code ? 'border-red-500' : 'border-gray-300'
                                            }`}
                                        />
                                        {formErrors.code && (
                                            <p className="text-red-500 text-xs mt-1">{formErrors.code}</p>
                                        )}
                                    </div>

                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            코드명 <span className="text-red-500">*</span>
                                        </label>
                                        <input
                                            type="text"
                                            value={formData.name}
                                            onChange={(e) => handleInputChange('name', e.target.value)}
                                            placeholder="예: 연차"
                                            className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                                                formErrors.name ? 'border-red-500' : 'border-gray-300'
                                            }`}
                                        />
                                        {formErrors.name && (
                                            <p className="text-red-500 text-xs mt-1">{formErrors.name}</p>
                                        )}
                                    </div>
                                </div>

                                <div className="flex justify-end space-x-3 mt-6">
                                    <button
                                        onClick={closeModal}
                                        className="px-4 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300 focus:outline-none focus:ring-2 focus:ring-gray-500"
                                    >
                                        취소
                                    </button>
                                    <button
                                        onClick={handleSave}
                                        disabled={loading}
                                        className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
                                    >
                                        {loading ? '저장 중...' : modalMode === 'create' ? '추가' : '수정'}
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                )}

                {/* 로딩 오버레이 */}
                {loading && (
                    <div className="fixed inset-0 bg-gray-600 bg-opacity-50 flex items-center justify-center z-40">
                        <div className="bg-white rounded-lg p-6 shadow-lg">
                            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
                            <p className="text-gray-600 mt-2">처리 중...</p>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};