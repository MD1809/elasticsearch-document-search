import React, { useState } from 'react';
import axios from 'axios';
import './SearchBar.css';

const SearchBar = ({ setResults, setLoading }) => {
    const [keyword, setKeyword] = useState('');
    const [searchTime, setSearchTime] = useState(0);

    const handleSearch = async (e) => {
        e.preventDefault();
        if (!keyword.trim()) return;

        setLoading(true);
        const startTime = performance.now();

        try {
            const response = await axios.get(`http://localhost:8080/api/search`, {
                params: { keyword: keyword }
            });

            const rawData = response.data;
            if (rawData && rawData.hits && rawData.hits.hits) {
                setResults(rawData.hits.hits);
            } else {
                setResults([]);
            }
        } catch (error) {
            console.error('Lỗi khi gọi API tìm kiếm:', error);
            alert('Hệ thống phân tán đang gặp sự cố hoặc Backend Spring Boot đã bị ngắt kết nối!');
            setResults([]);
        } finally {
            const endTime = performance.now();
            setSearchTime((endTime - startTime).toFixed(0));
            setLoading(false);
        }
    };

    return (
        <div className="search-container">
            <form onSubmit={handleSearch} className="search-box">
                <input
                    type="text"
                    placeholder="Nhập từ khóa tìm kiếm toàn văn (Ví dụ: phân tán, cấu trúc dữ liệu, vũ trụ...)"
                    value={keyword}
                    onChange={(e) => setKeyword(e.target.value)}
                    className="search-input"
                />
                <button type="submit" className="search-button">
                    Tìm Kiếm
                </button>
            </form>
            {searchTime > 0 && (
                <p className="search-time-text">
                    Thời gian phản hồi hệ thống: <strong>{searchTime} ms</strong>
                </p>
            )}
        </div>
    );
};

export default SearchBar;