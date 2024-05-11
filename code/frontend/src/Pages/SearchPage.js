import React, {useState } from 'react';
import '../Styles/style.css'
import Owl from '../Styles/Owl.png';
import searchingOwl from '../Styles/searchingOwl.gif';
import SearchBar from "../Components/SearchBar.jsx";


function SearchPage() {
    const [searching, setSearching] = useState(false);
    const loadImg = async() => {
        setSearching(true);
        setTimeout(() => { 
            setSearching(false) }, 3400);
    }

    return (
        <div className='main-container'>
            {
                searching===true ?
                    <iframe src={searchingOwl} width={432} height={432} frameBorder="0" allowFullScreen></iframe>
                    : <img src={Owl} alt="Owl" height={432} width={432} />
            }
            <SearchBar onSuggest={loadImg}/>
        </div>
    );
}
export default SearchPage;
