import React, { useEffect, useState } from 'react';
import Owl from '../Styles/Owl.png';
import searchingOwl from '../Styles/searchingOwl.gif';
import SearchBar from "../Components/SearchBar.jsx";
// import axios from 'axios';

function SearchPage() {
    const [searching, setSearching] = useState(0);
    const loadImg = async() => {
        setSearching(-1);
        setTimeout(() => { 
            setSearching(0) }, 3400);
    }

    return (
        <div style={styles.mainContainer}>
            {
                searching==-1 ?
                    <iframe src={searchingOwl} width={648} height={648} frameBorder="0" allowFullScreen></iframe>
                    : <img src={Owl} alt="Owl" height={648} width={648} />
            }
            <SearchBar onSuggest={loadImg}/>
        </div>
    );
}

const styles = {
    mainContainer: {
        backgroundColor: "#2B2012",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "start",
        height: "105vh",
        gap: "10px",
        paddingTop: "5vh"
    },
    buttonsContainer: {
        marginTop: "20px",
        display: "flex",
        flexDirection: "row",
        justifyContent: "center",
        gap: "20px"
    },
    button: {
        borderRadius: "25px",
        width: "100px",
        margin: "0 auto",
        backgroundColor: "#E8CFC1",
        height: "50px",
        fontWeight: "bolder",
        fontSize: "larger",
        color: "#2B2012",
        cursor: "pointer",
    }
};

export default SearchPage;
