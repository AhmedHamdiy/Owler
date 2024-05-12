import React from 'react';
import '../Styles/style.css'
import Owl from '../Styles/img/Owl.svg';
import Eye from '../Styles/img/owlEye.png';
import SearchBar from "../Components/SearchBar.jsx";
import Footer from "../Components/Footer.jsx";

function SearchPage() {

    const loadImg = async (queryLength) => {
        const leftEye = document.querySelector('.Left-Eye');
        const rightEye = document.querySelector('.Right-Eye');
        const leftEyeX = Math.min(727,(715 + (queryLength/5)));
        const rightEyeX = Math.min(800,(788 + (queryLength/5)));
        leftEye.style.left = `${leftEyeX}px`;
        rightEye.style.left = `${rightEyeX}px`;
    }

    return (
        <div className='main-container'>
            <div className='owl-img-main-page'>
                <img src={Owl} alt="Owl" height={350} width={350} />
                <img className='Left-Eye' alt='left-eye' src={Eye}/>
                <img className='Right-Eye' alt='right-eye' src={Eye}/>
            </div>
            <div className='search-page-search-bar'>
                <SearchBar onSuggest={loadImg} />
            </div>
            <Footer/>
        </div>
    );
}
export default SearchPage;
