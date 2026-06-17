import React, { useState } from 'react';
import axios from 'axios';
import './IngestionPanel.css';

const IngestionPanel = () => {
    const [file, setFile] = useState(null);
    const [loading, setLoading] = useState(false);
    const [statusMessage, setStatusMessage] = useState('');
    const [isError, setIsError] = useState(false);

    const handleFileChange = (e) => {
        setFile(e.target.files[0]);
        setStatusMessage('');
        setIsError(false);
    };

    const handleUpload = async (e) => {
        e.preventDefault();
        if (!file) {
            setIsError(true);
            setStatusMessage('Vui lòng chọn một file Excel (.xlsx) trước!');
            return;
        }

        const formData = new FormData();
        formData.append('file', file);

        setLoading(true);
        setIsError(false);
        setStatusMessage('Hệ thống phân tán đang bóc tách và nạp dữ liệu đa luồng...');

        try {
            const response = await axios.post('http://localhost:8080/api/ingest-file', formData, {
                headers: { 'Content-Type': 'multipart/form-data' },
            });
            setIsError(false);
            setStatusMessage(`Thông báo: ${response.data}`);
        } catch (error) {
            console.error('Lỗi nạp dữ liệu:', error);
            setIsError(true);
            setStatusMessage('Thất bại khi nạp dữ liệu. Vui lòng kiểm tra kết nối tới Backend Spring Boot!');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="ingestion-panel">
            <h3 className="ingestion-title">Hệ Thống Nạp Dữ Liệu Phân Tán Song Song</h3>
            <p className="ingestion-subtext">Hỗ trợ tải lên file Excel (.xlsx) chứa danh sách tài liệu, sách để băm nhỏ dữ liệu đánh chỉ mục.</p>
            
            <form onSubmit={handleUpload} className="ingestion-form">
                <input 
                    type="file" 
                    accept=".xlsx, .xls" 
                    onChange={handleFileChange} 
                    className="ingestion-file-input"
                    disabled={loading}
                />
                <button 
                    type="submit" 
                    className={loading ? "ingestion-button-disabled" : "ingestion-button"}
                    disabled={loading}
                >
                    {loading ? 'Đang nạp...' : 'Nạp dữ liệu'}
                </button>
            </form>

            {statusMessage && (
                <div className={isError ? "ingestion-error-alert" : "ingestion-success-alert"}>
                    {statusMessage}
                </div>
            )}
        </div>
    );
};

export default IngestionPanel;