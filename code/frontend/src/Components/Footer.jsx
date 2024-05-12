import React from 'react';
import '../Styles/style.css';
import Ahmed from '../Styles/img/profile-images/Ahmed.png';
import Khalid from '../Styles/img/profile-images/Khalid.png';
import Mariam from '../Styles/img/profile-images/Mariam.png'; 
import Rabie from '../Styles/img/profile-images/Rabie.png';

function Footer() {
    const Info = [{
        path: Ahmed,
        name: 'Hamdy',
        github: 'AhmedHamdiy'
    }, {
        path: Khalid,
        name:'Khalid',
        github: 'jpassica'
    }, {
        path: Mariam,
        name: 'Mariam',
        github: 'Mariam-Amin12'
    }, {
        path: Rabie,
        name: 'Rabie',
        github: 'mostafarabie5'
    }];

    const navigateToGitHub = (username) => {
        window.open(`https://github.com/${username}`, '_blank');
    };

    return (
            <footer>
                {Info.map((person, index) => (
                    <div key={index} className="person-container"
                        data-name={person.name} onClick={() => navigateToGitHub(person.github)}>
                        <img src={person.path}
                            alt={person.name} className="profile-img"/>
                    </div>
                ))}
            </footer>
    );
}

export default Footer;
