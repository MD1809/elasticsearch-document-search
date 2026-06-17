import React from 'react';
import './ResultList.css';

const ResultList = ({ results, loading }) => {
    
    if (loading) {
        return (
            <div className="results-container">
                <div className="no-results">
                    Đang tìm kiếm và kết nối tới cụm phân tán...
                </div>
            </div>
        );
    }

    if (!results || results.length === 0) {
        return (
            <div className="results-container">
                <div className="no-results">
                    Không tìm thấy tài liệu phù hợp hoặc hệ thống chưa có dữ liệu.
                </div>
            </div>
        );
    }

    return (
        <div className="results-container">
            <div className="results-count">
                Tìm thấy <strong>{results.length}</strong> kết quả phù hợp trong cụm máy chủ.
            </div>
            
            <div className="results-list">
                {results.map((hit) => {
                    const book = hit._source; 
                    return (
                        <div key={hit._id} className="result-card">
                            <h4 className="result-title">{book.title}</h4>
                            
                            <div className="result-meta">
                                <span>
                                    <span className="meta-label">Tác giả:</span> {book.author}
                                </span>
                                <span>
                                    <span className="meta-label">Thể loại:</span> {book.category}
                                </span>
                                <span>
                                    <span className="meta-label">Độ chính xác (Score):</span> {hit._score ? hit._score.toFixed(4) : 'N/A'}
                                </span>
                            </div>
                            
                            <p className="result-content">{book.content}</p>
                        </div>
                    );
                })}
            </div>
        </div>
    );
};

export default ResultList;